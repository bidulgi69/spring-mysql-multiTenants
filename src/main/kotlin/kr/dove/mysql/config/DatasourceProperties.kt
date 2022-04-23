package kr.dove.mysql.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.datasource.master")
data class MasterDataSourceProperties(
    val url: String,
    val username: String,
    val password: String,
) {
    companion object {
        const val KEY = "master"
        const val transactionManager = "masterTransactionManager"
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.datasource.slave")
data class SlaveDataSourceProperties(
    val url: String,
    val username: String,
    val password: String,
) {
    companion object {
        const val KEY = "slave"
        const val transactionManager = "slaveTransactionManager"
    }
}
