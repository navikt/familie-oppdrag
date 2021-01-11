package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.oppdragId
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.repository.OppdragRepository
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
@ContextConfiguration(initializers = [Containers.PostgresSQLInitializer::class, Containers.MQInitializer::class])
@SpringBootTest(classes = [TestConfig::class], properties = ["spring.cloud.vault.enabled=false"])
@EnableJms
@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
@Testcontainers
internal class OppdragControllerIntegrasjonTest {

    @Autowired lateinit var oppdragService: OppdragService
    @Autowired lateinit var oppdragLagerRepository: OppdragRepository

    companion object {

        @Container var postgreSQLContainer = Containers.postgreSQLContainer
        @Container var ibmMQContainer = Containers.ibmMQContainer
    }

    @Test
    fun test_skal_lagre_oppdrag_for_utbetalingoppdrag() {

        val mapper = OppdragMapper()
        val oppdragController = OppdragController(oppdragService, mapper)

        val utbetalingsoppdrag = utbetalingsoppdragMedTilfeldigAktoer()
        oppdragController.sendOppdrag(utbetalingsoppdrag)

        var oppdragStatus: OppdragStatus

        do {
            val oppdrag = oppdragLagerRepository.hentOppdrag(utbetalingsoppdrag.oppdragId.fagsystem,
                                                             utbetalingsoppdrag.oppdragId.personIdent,
                                                             utbetalingsoppdrag.oppdragId.behandlingsId)

            oppdragStatus = oppdrag.status
        } while (oppdragStatus == OppdragStatus.LAGT_PÅ_KØ)

        assertEquals(OppdragStatus.KVITTERT_OK, oppdragStatus)
    }
}
