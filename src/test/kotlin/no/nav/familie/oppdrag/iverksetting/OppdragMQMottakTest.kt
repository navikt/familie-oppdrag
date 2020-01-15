package no.nav.familie.oppdrag.iverksetting

import io.mockk.*
import no.nav.familie.kontrakter.felles.oppdrag.Opphør
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.familie.oppdrag.repository.OppdragProtokollStatus
import no.nav.familie.oppdrag.repository.somOppdragProtokoll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.env.Environment
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.jms.TextMessage
import kotlin.test.assertEquals


class OppdragMQMottakTest {

    lateinit var oppdragMottaker: OppdragMottaker

    val localDateTimeNow = LocalDateTime.now()
    val localDateNow = LocalDate.now()


    val utbetalingsoppdragMedTilfeldigAktoer = Utbetalingsoppdrag(
            Utbetalingsoppdrag.KodeEndring.NY,
            "TEST",
            "SAKSNR",
            UUID.randomUUID().toString(), // Foreløpig plass til en 50-tegn string og ingen gyldighetssjekk
            "SAKSBEHANDLERID",
            localDateTimeNow,
            listOf(Utbetalingsperiode(false,
                                      Opphør(localDateNow),
                                      localDateNow,
                                      "KLASSE A",
                                      localDateNow,
                                      localDateNow,
                                      BigDecimal.ONE,
                                      Utbetalingsperiode.SatsType.MND,
                                      "UTEBETALES_TIL",
                                      1))
    )

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
        val oppdragProtokoll = utbetalingsoppdragMedTilfeldigAktoer.somOppdragProtokoll

        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()

        every { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) } returns
                listOf(oppdragProtokoll)

        every { oppdragProtokollRepository.lagreOppdrag(any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, devEnv)

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) }
        verify(exactly = 1) { oppdragProtokollRepository.lagreOppdrag(any<OppdragProtokoll>()) }

    }

    @Test
    fun skal_logge_error_hvis_det_finnes_to_identiske_oppdrag_i_databasen() {
        val oppdragProtokoll = utbetalingsoppdragMedTilfeldigAktoer.somOppdragProtokoll

        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()

        every { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) } returns
                listOf(oppdragProtokoll, oppdragProtokoll)

        every { oppdragProtokollRepository.lagreOppdrag(any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) }
        verify(exactly = 1) { oppdragMottaker.LOG.error(any()) }
        verify(exactly = 0) { oppdragProtokollRepository.lagreOppdrag(any<OppdragProtokoll>()) }
    }

    @Test
    fun skal_logge_error_hvis_oppdraget_mangler_i_databasen() {
        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()

        every { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) } returns emptyList()
        every { oppdragProtokollRepository.lagreOppdrag(any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) }
        verify(exactly = 1) { oppdragMottaker.LOG.error(any()) }
        verify(exactly = 0) { oppdragProtokollRepository.lagreOppdrag(any<OppdragProtokoll>()) }
    }

    @Test
    fun skal_logge_warn_hvis_oppdrag_i_databasen_har_uventet_status() {
        val oppdragProtokoll = utbetalingsoppdragMedTilfeldigAktoer.somOppdragProtokoll

        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()

        every { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) } returns
                listOf(oppdragProtokoll.copy(status = OppdragProtokollStatus.KVITTERT_OK))

        every { oppdragProtokollRepository.lagreOppdrag(any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.warn(any()) } just Runs

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) }
        verify(exactly = 1) { oppdragMottaker.LOG.warn(any()) }
        verify(exactly = 1) { oppdragProtokollRepository.lagreOppdrag(any<OppdragProtokoll>()) }
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