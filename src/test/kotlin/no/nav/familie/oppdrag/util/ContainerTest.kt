package no.nav.familie.oppdrag.util

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer

@Configuration
@ComponentScan("no.nav.familie.oppdrag") class TestConfig

object Containers {

    var postgreSQLContainer = MyPostgreSQLContainer("postgres:latest")
            .withDatabaseName("familie-oppdrag")
            .withUsername("postgres")
            .withPassword("test")
            .withExposedPorts(5432)

    var ibmMQContainer = MyGeneralContainer("ibmcom/mq")
            .withEnv("LICENSE","accept")
            .withEnv("MQ_QMGR_NAME","QM1")
            .withExposedPorts(1414)

    class MyPostgreSQLContainer(imageName: String) : PostgreSQLContainer<MyPostgreSQLContainer>(imageName)
    class MyGeneralContainer(imageName: String) : GenericContainer<MyGeneralContainer>(imageName)

    class PostgresSQLInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.jdbcUrl,
                    "spring.datasource.username=" + postgreSQLContainer.username,
                    "spring.datasource.password=" + postgreSQLContainer.password
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    class MQInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                    "oppdrag.mq.port=" + ibmMQContainer.getMappedPort(1414),
                    "oppdrag.mq.queuemanager=QM1",
                    "oppdrag.mq.send=DEV.QUEUE.1",
                    "oppdrag.mq.mottak=DEV.QUEUE.1",
                    "oppdrag.mq.channel=DEV.ADMIN.SVRCONN",
                    "oppdrag.mq.hostname=localhost",
                    "oppdrag.mq.user=admin",
                    "oppdrag.mq.password: passw0rd",
                    "oppdrag.mq.enabled: true"
            ).applyTo(configurableApplicationContext.environment)
        }
    }

}
