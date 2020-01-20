package no.nav.familie.oppdrag.rest

import no.nav.familie.oppdrag.domene.id
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.familie.oppdrag.repository.OppdragProtokollStatus
import no.nav.familie.oppdrag.service.OppdragService
import no.nav.familie.oppdrag.util.Containers
import no.nav.familie.oppdrag.util.TestConfig
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.annotation.EnableJms
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals


@ActiveProfiles("dev")
@ContextConfiguration(initializers = arrayOf(Containers.PostgresSQLInitializer::class,Containers.MQInitializer::class))
@SpringBootTest(classes = [TestConfig::class], properties = ["spring.cloud.vault.enabled=false"])
@EnableJms
@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
@Testcontainers
internal class OppdragControllerIntegrasjonTest {

    @Autowired lateinit var oppdragService: OppdragService
    @Autowired lateinit var oppdragProtokollRepository: OppdragProtokollRepository

    companion object {
        @Container var postgreSQLContainer = Containers.postgreSQLContainer
        @Container var ibmMQContainer = Containers.ibmMQContainer
    }

    @Test
    fun test_skal_lagre_oppdragprotokoll_for_utbetalingoppdrag() {

        val mapper = OppdragMapper()
        val oppdragController = OppdragController(oppdragService, mapper)

        val utbetalingsoppdrag = utbetalingsoppdragMedTilfeldigAktoer()
        oppdragController.sendOppdrag(utbetalingsoppdrag)

        var oppdragStatus: OppdragProtokollStatus

        do {
            val oppdrag = oppdragProtokollRepository.hentOppdrag(utbetalingsoppdrag.id)

            oppdragStatus = oppdrag.status
        } while (oppdragStatus == OppdragProtokollStatus.LAGT_PÅ_KØ)

        assertEquals( OppdragProtokollStatus.KVITTERT_UKJENT,oppdragStatus)
    }
}