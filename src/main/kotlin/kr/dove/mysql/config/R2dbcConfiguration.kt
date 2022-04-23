package kr.dove.mysql.config

import com.github.jasync.r2dbc.mysql.JasyncConnectionFactory
import com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory
import com.github.jasync.sql.db.mysql.util.URLParser
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@Configuration
@ConfigurationPropertiesScan(basePackages = ["kr.dove.mysql.config"])
@EnableR2dbcRepositories(basePackages = ["kr.dove.mysql.persistence"])
class R2dbcConfiguration(
    val masterDataSourceProperties: MasterDataSourceProperties,
    val slaveDataSourceProperties: SlaveDataSourceProperties,
) : AbstractR2dbcConfiguration() {

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val multiTenantRoutingConnectionFactory = MultiTenantRoutingConnectionFactory()

        val factories: Map<String, ConnectionFactory> = mapOf(
            MasterDataSourceProperties.KEY to masterConnectionFactory(),
            SlaveDataSourceProperties.KEY to slaveConnectionFactory()
        )

        multiTenantRoutingConnectionFactory.setDefaultTargetConnectionFactory(masterConnectionFactory())
        multiTenantRoutingConnectionFactory.setTargetConnectionFactories(factories)
        return multiTenantRoutingConnectionFactory
    }

    @Bean
    fun ddlInitializer(
        @Qualifier("masterConnectionFactory") connectionFactory: ConnectionFactory
    ): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        initializer.setDatabasePopulator(
            ResourceDatabasePopulator(
                ClassPathResource("ddl.sql")
            )
        )
        return initializer
    }

    @Bean
    fun masterConnectionFactory() =
        generateConnectionFactory(masterDataSourceProperties.url, masterDataSourceProperties.username, masterDataSourceProperties.password)

    @Bean
    fun slaveConnectionFactory() =
        generateConnectionFactory(slaveDataSourceProperties.url, slaveDataSourceProperties.username, slaveDataSourceProperties.password)

    @Bean
    fun masterTransactionManager(
        @Qualifier("masterConnectionFactory") connectionFactory: ConnectionFactory
    ) = R2dbcTransactionManager(connectionFactory)

    @Bean
    fun slaveTransactionManager(
        @Qualifier("slaveConnectionFactory") connectionFactory: ConnectionFactory
    ) = R2dbcTransactionManager(connectionFactory)

    private fun generateConnectionFactory(url: String, username: String, password: String): ConnectionFactory {
        val properties = URLParser.parseOrDie(url)
        return JasyncConnectionFactory(MySQLConnectionFactory(
            com.github.jasync.sql.db.Configuration(
                username = username,
                password = password,
                host = properties.host,
                port = properties.port,
                database = properties.database,
                charset = properties.charset,
                ssl = properties.ssl
            ))
        )
    }
}