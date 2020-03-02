package no.nav.familie.oppdrag.iverksetting

import io.mockk.*
import no.nav.familie.oppdrag.repository.OppdragLager
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.repository.OppdragStatus
import no.nav.familie.oppdrag.repository.somOppdragLager
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.env.Environment
import javax.jms.TextMessage
import kotlin.test.assertEquals


class OppdragMQMottakTest {

    lateinit var oppdragMottaker: OppdragMottaker

    val devEnv: Environment
        get() {
            val env = mockk<Environment>()
            every { env.activeProfiles } returns arrayOf("dev")
            return env
        }


    @BeforeEach
    fun setUp() {
        val env = mockk<Environment>()
        val oppdragLagerRepository = mockk<OppdragLagerRepository>()
        every { env.activeProfiles } returns arrayOf("dev")

        oppdragMottaker = OppdragMottaker(oppdragLagerRepository, env)
    }

    @Test
    fun skal_tolke_kvittering_riktig_ved_OK() {
        val kvittering: String = lesKvittering("kvittering-akseptert.xml")
        val statusFraKvittering = oppdragMottaker.lesKvittering(kvittering).status
        assertEquals(Status.OK, statusFraKvittering)
    }

    @Test
    fun skal_tolke_kvittering_riktig_ved_feil() {
        val kvittering: String = lesKvittering("kvittering-avvist.xml")
        val statusFraKvittering = oppdragMottaker.lesKvittering(kvittering).status
        assertEquals(Status.AVVIST_FUNKSJONELLE_FEIL, statusFraKvittering)
    }

    @Test
    fun skal_lagre_status_og_mmel_fra_kvittering() {
        val oppdragLager = utbetalingsoppdragMedTilfeldigAktoer().somOppdragLager

        val oppdragLagerRepository = mockk<OppdragLagerRepository>()

        every { oppdragLagerRepository.hentOppdrag(any()) } returns
                oppdragLager

        every { oppdragLagerRepository.oppdaterStatus(any(),any()) } just Runs
        every { oppdragLagerRepository.oppdaterKvitteringsmelding(any(), any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragLagerRepository.hentOppdrag(any()) }
        verify(exactly = 1) { oppdragLagerRepository.oppdaterStatus(any(),any()) }
        verify(exactly = 1) { oppdragLagerRepository.oppdaterKvitteringsmelding(any(), any()) }
    }

    @Test
    fun skal_logge_error_hvis_det_finnes_to_identiske_oppdrag_i_databasen() {

        val oppdragLagerRepository = mockk<OppdragLagerRepository>()

        every { oppdragLagerRepository.hentOppdrag(any()) } throws Exception()

        every { oppdragLagerRepository.opprettOppdrag(any(), any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        assertThrows<Exception> { oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage) }
        verify(exactly = 0) { oppdragLagerRepository.opprettOppdrag(any<OppdragLager>(), any()) }
    }

    @Test
    fun skal_logge_error_hvis_oppdraget_mangler_i_databasen() {
        val oppdragLagerRepository = mockk<OppdragLagerRepository>()

        every { oppdragLagerRepository.hentOppdrag(any()) } throws Exception()
        every { oppdragLagerRepository.opprettOppdrag(any(), any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        assertThrows<Exception> { oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage) }
        verify(exactly = 0) { oppdragLagerRepository.opprettOppdrag(any<OppdragLager>(), any()) }
    }

    @Test
    fun skal_logge_warn_hvis_oppdrag_i_databasen_har_uventet_status() {
        val oppdragLager = utbetalingsoppdragMedTilfeldigAktoer().somOppdragLager

        val oppdragLagerRepository = mockk<OppdragLagerRepository>()

        every { oppdragLagerRepository.hentOppdrag(any()) } returns
                oppdragLager.copy(status = OppdragStatus.KVITTERT_OK)

        every { oppdragLagerRepository.oppdaterStatus(any(),OppdragStatus.KVITTERT_OK) } just Runs
        every { oppdragLagerRepository.oppdaterKvitteringsmelding(any(), any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.warn(any()) } just Runs
        every { oppdragMottaker.LOG.debug(any()) } just Runs

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragLagerRepository.hentOppdrag(any()) }
        verify(exactly = 1) { oppdragMottaker.LOG.warn(any()) }
        verify(exactly = 1) { oppdragLagerRepository.oppdaterStatus(any(),any()) }
    }

    private fun lesKvittering(filnavn: String): String {
        return this::class.java.getResourceAsStream("/$filnavn").bufferedReader().use { it.readText() }
    }

    val String.fraRessursSomTextMessage: TextMessage
        get() {
            val textMessage = mockk<TextMessage>()
            every { textMessage.text } returns lesKvittering(this)
            return textMessage
        }
}