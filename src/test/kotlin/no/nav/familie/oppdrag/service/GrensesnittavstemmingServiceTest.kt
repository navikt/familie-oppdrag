package no.nav.familie.oppdrag.service

import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.util.Containers
import no.nav.familie.oppdrag.util.TestConfig
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.annotation.EnableJms
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@ActiveProfiles("dev")
@ContextConfiguration(initializers = [Containers.PostgresSQLInitializer::class, Containers.MQInitializer::class])
@SpringBootTest(classes = [TestConfig::class], properties = ["spring.cloud.vault.enabled=false"])
@EnableJms
@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
@Testcontainers
class GrensesnittavstemmingServiceTest {

    @Autowired lateinit var grensesnittavstemmingService: GrensesnittavstemmingService

    @Autowired lateinit var oppdragLagerRepository: OppdragLagerRepository

    companion object {

        @Container var postgreSQLContainer = Containers.postgreSQLContainer

        @Container var ibmMQContainer = Containers.ibmMQContainer
    }

    @Test
    fun `skal sende grensesnitt`() {
        val oppdrag1 = TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer()
        //  oppdragLagerRepository.opprettOppdrag()
    }
}
