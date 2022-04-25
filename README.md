# spring-mysql-multiTenants
MySQL 8 with spring boot + r2dbc + <a href="https://github.com/jasync-sql/jasync-sql">jasync driver</a> + mysql replication
<br><br>
If you apply CQRS pattern to distribute the database load, it may be necessary to connect to more than one database on one server.
<br>
At this time, the `AbstractRoutingConnectionFactory` supported by R2dbc can be used to determine the DataSource when transaction occurs.
<br><br>
<img src="https://user-images.githubusercontent.com/17774927/164944822-3930246d-f70d-40a8-9f56-afdef84c9da4.png">
<br><br>

## Stacks
<div>
  <img src="https://img.shields.io/badge/kotlin-7f5eff?style=for-the-badge&logo=kotlin&logoColor=white">
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
</div>
<br><br>

## How does it work
Save the Datasources you want to connect in form of `[Key, ConnectionFactory]`<br>
The key returned by the `detectCurrentLookupKey` function is used to select the `ConnectionFactory`.

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
<br>
When a transaction occurs, the function below is called.<br>
`@Transaction` annotations can be used to hand over variables that can determine the key.

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
<br>
Based on the annotated location of `@Transactional`,<br>
you can receive "transaction name" value from `transactionManager`.<br>
It would be something like this... ---> kr.dove.mysql.service.cities (package name + method or class name)<br><br>
And you can also receive readOnly property from `transactionManager`. (.isCurrentTransactionReadOnly)

    @Transactional(
        value = SlaveDataSourceProperties.transactionManager,
        readOnly = true
    )

<img src="https://user-images.githubusercontent.com/17774927/164947212-17562b66-8ea3-4ba0-a559-27aef43ef134.png">
<br><br>

## Run
- Fire up MySQL servers

      docker-compose up -d

- Replicate mysql (master & slave)
  1. Access to Master, Slave server
      
          docker exec -it mysql-master /bin/bash  # master
          docker exec -it mysql-slave /bin/bash   # slave
      
  2. Before starting replication, you need to check out the <a href="https://stackoverflow.com/questions/21729832/same-id-error-when-i-try-to-replicate-databases">server_id</a> variables in each server.

          show variables like 'server_id';

  3. [Optinal] If two servers have the same server_id, sets the server_id for one server differently.
  
          set global server_id=different value;

  4. Creates a user for communication between master and slave. <br>
     You need to make sure that [[authentication_policy=mysql_native_password]] is set in both cnf files. (Mysql 8)
        
          # Master container
          # bash
          mysql -uroot -proot
          
          # mysql
          mysql> CREATE USER 'slave'@'%' IDENTIFIED BY 'password';
          Query OK, 0 rows affected (0.00 sec)
          mysql> GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'slave'@'%';
          Query OK, 0 rows affected (0.00 sec)
  
  5. Set connection host parameters
  
          # Slave container
          # bash
          mysql -uroot -proot
          
          # mysql
          mysql> change master to 
          master_host='172.22.0.3',
          master_user='slave',
          master_password='password',
          master_port=3306,
          master_log_file='mysql-bin.000003',
          master_log_pos=157;

  6. Some variables are from...<br>
     - The value of `MASTER_HOST` can be found<br> by inquiring about the docker network to which the current containers belong.
        
            docker inspect f997b87515bb(network name) | jq
            [
              {
                "Name": "mysql_net-mysql",
                "Containers": {
                  "913e258e271eca08249611c813b7efd84756ac1d4b3c5254c42e9dbfcab8e9d3": {
                    "Name": "mysql-master",
                    "EndpointID": "7951e87e0ab78728a8db387c180375e19b0aa8136593fbe7793ce932350de162",
                    "MacAddress": "02:42:ac:16:00:03",
                    "IPv4Address": "172.22.0.3/16", // you need to set this value as MASTER_HOST
                    "IPv6Address": ""
                  },
                  "c64a8e5ec49e748f899c1b2158ea19a4ae527e0cc8422a4d854501e65d253c74": {
                    "Name": "mysql-slave",
                    "EndpointID": "c1608ce02b2e834721b2902257e7e7a65fc5bfce75f4fb447df702f905ccf300",
                    "MacAddress": "02:42:ac:16:00:02",
                    "IPv4Address": "172.22.0.2/16",
                    "IPv6Address": ""
                  }
                },
                "Options": {},
                "Labels": {
                  "com.docker.compose.network": "net-mysql",
                  "com.docker.compose.project": "mysql",
                  "com.docker.compose.version": "1.29.0"
                }
              }
            ]
            
     - `MASTER_LOG_FILE` and `MASTER_LOG_POS` values can be found<br> by executing this command in the master MySQL server.

            # Master container
            # mysql
            show master status;
            +------------------+----------+--------------+------------------+-------------------+
            | File             | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
            +------------------+----------+--------------+------------------+-------------------+
            | mysql-bin.000003 |      157 |              |                  |                   |
            +------------------+----------+--------------+------------------+-------------------+
            1 row in set (0.03 sec)

  7. Enable master-slave replication 

            # Slave container
            # mysql
            // Turn on master-slave mode 
            start slave;
            // View the slave connection status 
            show slave status \G;

  8. If you found
      
            Slave_IO_Running: Yes,
            Slave_SQL_Running: Yes,
            
  <strong>We're all set!</strong><br><br>
  
- Build & Run java application

      ./gradlew bootjar && java -jar /build/libs/*.jar &
            
<br>

## References

- https://javamana.com/2022/04/202204021539428098.html
- https://blog.daum.net/khere/36
- https://huisam.tistory.com/entry/routingDataSource
- https://tech.lezhin.com/2020/07/15/kotlin-webflux
