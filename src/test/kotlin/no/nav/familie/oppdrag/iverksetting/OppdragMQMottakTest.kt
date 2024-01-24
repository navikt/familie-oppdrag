package no.nav.familie.oppdrag.iverksetting

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.jms.TextMessage
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.oppdrag.repository.OppdragLager
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.repository.somKvitteringsinformasjon
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.env.Environment
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
        val kvitteringsinformasjon = utbetalingsoppdragMedTilfeldigAktoer().somKvitteringsinformasjon

        val oppdragLagerRepository = mockk<OppdragLagerRepository>()

        every { oppdragLagerRepository.hentKvitteringsinformasjon(any()) } returns
            listOf(kvitteringsinformasjon)

        every { oppdragLagerRepository.oppdaterStatus(any(), any()) } just Runs
        every { oppdragLagerRepository.oppdaterKvitteringsmelding(any(), any(), any(), any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragLagerRepository.hentKvitteringsinformasjon(any()) }
        verify(exactly = 1) { oppdragLagerRepository.oppdaterKvitteringsmelding(any(), any(), any(), any()) }
    }

    @Test
    fun skal_lagre_kvittering_på_riktig_versjon() {
        val oppdragLager = utbetalingsoppdragMedTilfeldigAktoer().somKvitteringsinformasjon.copy(status = OppdragStatus.KVITTERT_OK)
        val oppdragLagerV1 = utbetalingsoppdragMedTilfeldigAktoer().somKvitteringsinformasjon.copy(versjon = 1)

        val oppdragLagerRepository = mockk<OppdragLagerRepository>()

        every { oppdragLagerRepository.hentKvitteringsinformasjon(any()) } returns
            listOf(oppdragLager, oppdragLagerV1)

        every { oppdragLagerRepository.oppdaterStatus(any(), any(), any()) } just Runs
        every { oppdragLagerRepository.oppdaterKvitteringsmelding(any(), any(), any(), any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 0) { oppdragLagerRepository.oppdaterKvitteringsmelding(any(), any(), any(), 0) }
        verify(exactly = 1) { oppdragLagerRepository.oppdaterKvitteringsmelding(any(), any(), any(), 1) }
    }

    @Test
    fun skal_logge_error_hvis_det_finnes_to_identiske_oppdrag_i_databasen() {
        val oppdragLagerRepository = mockk<OppdragLagerRepository>()

        every { oppdragLagerRepository.hentKvitteringsinformasjon(any()) } throws Exception()

        every { oppdragLagerRepository.opprettOppdrag(any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        assertThrows<Exception> { oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage) }
        verify(exactly = 0) { oppdragLagerRepository.opprettOppdrag(any<OppdragLager>()) }
    }

    @Test
    fun skal_logge_error_hvis_oppdraget_mangler_i_databasen() {
        val oppdragLagerRepository = mockk<OppdragLagerRepository>()

        every { oppdragLagerRepository.hentKvitteringsinformasjon(any()) } throws Exception()
        every { oppdragLagerRepository.opprettOppdrag(any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        assertThrows<Exception> { oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage) }
        verify(exactly = 0) { oppdragLagerRepository.opprettOppdrag(any<OppdragLager>()) }
    }

    @Test
    fun skal_logge_warn_hvis_oppdrag_i_databasen_har_uventet_status() {
        val oppdragLager = utbetalingsoppdragMedTilfeldigAktoer().somKvitteringsinformasjon

        val oppdragLagerRepository = mockk<OppdragLagerRepository>()

        every { oppdragLagerRepository.hentKvitteringsinformasjon(any()) } returns
            listOf(oppdragLager.copy(status = OppdragStatus.KVITTERT_OK))

        every { oppdragLagerRepository.oppdaterStatus(any(), OppdragStatus.KVITTERT_OK) } just Runs
        every { oppdragLagerRepository.oppdaterKvitteringsmelding(any(), any(), any(), any()) } just Runs

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.warn(any()) } just Runs
        every { oppdragMottaker.LOG.debug(any()) } just Runs

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragLagerRepository.hentKvitteringsinformasjon(any()) }
        verify(exactly = 1) { oppdragMottaker.LOG.warn(any()) }
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
