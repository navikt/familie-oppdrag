package no.nav.familie.oppdrag

import no.nav.familie.oppdrag.config.ApplicationConfig
import org.springframework.boot.builder.SpringApplicationBuilder
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*

object DevPsqlMqLauncher {
    @JvmStatic
    fun main(args: Array<String>) {

        val psql = KPostgreSQLContainer("postgres")
                .withDatabaseName("familie-oppdrag")
                .withUsername("postgres")
                .withPassword("test")

        psql.start()

        val mq = KGenericContainer("ibmcom/mq")
                .withEnv("LICENSE","accept")
                .withEnv("MQ_QMGR_NAME","QM1")
                .withExposedPorts(1414,9443)

        mq.start()

        val properties = Properties()
        properties.put("spring.datasource.url",psql.jdbcUrl)
        properties.put("spring.datasource.username", psql.username)
        properties.put("spring.datasource.password", psql.password)

        properties.put("oppdrag.mq.port",mq.getMappedPort(1414))

         val app = SpringApplicationBuilder(ApplicationConfig::class.java)
                .profiles("dev_psql_mq")
                .properties(properties)

        app.run(*args)
    }
}

class KPostgreSQLContainer(imageName: String) : PostgreSQLContainer<KPostgreSQLContainer>(imageName)

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)