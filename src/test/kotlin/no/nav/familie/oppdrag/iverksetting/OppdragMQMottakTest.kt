package no.nav.familie.oppdrag.iverksetting

import io.mockk.*
import no.nav.familie.kontrakter.felles.oppdrag.Opphør
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.familie.oppdrag.repository.OppdragProtokollStatus
import no.nav.familie.oppdrag.repository.somOppdragProtokoll
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.env.Environment
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
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
        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()
        every { env.activeProfiles } returns arrayOf("dev")

        oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, env)
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
    fun skal_lagre_status_fra_kvittering() {
        val oppdragProtokoll = utbetalingsoppdragMedTilfeldigAktoer().somOppdragProtokoll

        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()

        every { oppdragProtokollRepository.hentOppdrag(any()) } returns
                oppdragProtokoll

        every { oppdragProtokollRepository.oppdaterStatus(any(),any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, devEnv)

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragProtokollRepository.hentOppdrag(any()) }
        verify(exactly = 1) { oppdragProtokollRepository.oppdaterStatus(any(),any()) }

    }

    @Test
    fun skal_logge_error_hvis_det_finnes_to_identiske_oppdrag_i_databasen() {
        val oppdragProtokoll = utbetalingsoppdragMedTilfeldigAktoer().somOppdragProtokoll

        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()

        every { oppdragProtokollRepository.hentOppdrag(any()) } throws Exception()

        every { oppdragProtokollRepository.opprettOppdrag(any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        assertThrows<Exception> { oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage) }
        verify(exactly = 0) { oppdragProtokollRepository.opprettOppdrag(any<OppdragProtokoll>()) }
    }

    @Test
    fun skal_logge_error_hvis_oppdraget_mangler_i_databasen() {
        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()

        every { oppdragProtokollRepository.hentOppdrag(any()) } throws Exception()
        every { oppdragProtokollRepository.opprettOppdrag(any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        assertThrows<Exception> { oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage) }
        verify(exactly = 0) { oppdragProtokollRepository.opprettOppdrag(any<OppdragProtokoll>()) }
    }

    @Test
    fun skal_logge_warn_hvis_oppdrag_i_databasen_har_uventet_status() {
        val oppdragProtokoll = utbetalingsoppdragMedTilfeldigAktoer().somOppdragProtokoll

        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()

        every { oppdragProtokollRepository.hentOppdrag(any()) } returns
                oppdragProtokoll.copy(status = OppdragProtokollStatus.KVITTERT_OK)

        every { oppdragProtokollRepository.oppdaterStatus(any(),OppdragProtokollStatus.KVITTERT_OK) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.warn(any()) } just Runs

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragProtokollRepository.hentOppdrag(any()) }
        verify(exactly = 1) { oppdragMottaker.LOG.warn(any()) }
        verify(exactly = 1) { oppdragProtokollRepository.oppdaterStatus(any(),any()) }
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