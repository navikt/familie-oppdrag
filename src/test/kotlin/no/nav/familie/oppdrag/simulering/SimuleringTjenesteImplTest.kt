package no.nav.familie.oppdrag.simulering

import no.nav.familie.oppdrag.repository.SimuleringLagerTjeneste
import no.nav.familie.oppdrag.simulering.util.lagTestUtbetalingsoppdragForFGBMedEttBarn
import no.nav.familie.oppdrag.util.Containers
import no.nav.familie.oppdrag.util.TestConfig
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals

@ActiveProfiles("dev")
@ContextConfiguration(initializers = arrayOf(Containers.PostgresSQLInitializer::class, Containers.MQInitializer::class))
@SpringBootTest(classes = [TestConfig::class], properties = ["spring.cloud.vault.enabled=false"])
@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
@Testcontainers
internal class SimuleringTjenesteImplTest {

    @Autowired lateinit var simuleringLagerTjeneste: SimuleringLagerTjeneste
    @Autowired lateinit var simuleringTjeneste: SimuleringTjenesteImpl

    companion object {
        @Container var postgreSQLContainer = Containers.postgreSQLContainer
        @Container var ibmMQContainer = Containers.ibmMQContainer
    }

    @Test
    fun skal_lagre_request_og_respons() {
        val utbetalingsoppdrag = lagTestUtbetalingsoppdragForFGBMedEttBarn()

        val simuleringResultat = simuleringTjeneste.utførSimuleringOghentDetaljertSimuleringResultat(utbetalingsoppdrag)

        assertNotNull(simuleringResultat)

        val alleLagretSimuleringsLager = simuleringLagerTjeneste.finnAlleSimuleringsLager()
        assertEquals(1, alleLagretSimuleringsLager.size)
        val simuleringsLager = alleLagretSimuleringsLager[0]
        assertNotNull(simuleringsLager.requestXml)
        assertNotNull(simuleringsLager.responseXml)
    }
}
