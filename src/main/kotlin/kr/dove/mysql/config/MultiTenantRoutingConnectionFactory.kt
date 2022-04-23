package kr.dove.mysql.config

import org.slf4j.LoggerFactory
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory
import org.springframework.transaction.reactive.TransactionSynchronizationManager
import reactor.core.publisher.Mono

class MultiTenantRoutingConnectionFactory : AbstractRoutingConnectionFactory() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun determineCurrentLookupKey(): Mono<Any> {
        return TransactionSynchronizationManager.forCurrentTransaction()
            .flatMap { manager ->
                Mono.fromCallable {
                    logger.info("Current Transaction from: ${manager.currentTransactionName}")

                    //  lookup based on "readOnly" property of @Transactional
                    if (manager.isCurrentTransactionReadOnly) SlaveDataSourceProperties.KEY
                    else MasterDataSourceProperties.KEY

                    // lookup based on transaction name (Name of method or class with @Transactional annotation attached to)
                    /*
                    if (manager.currentTransactionName ?. contains(".post") == true) MasterDataSourceProperties.KEY
                    else SlaveDataSourceProperties.KEY
                     */
                }
            }
    }
}