package no.nav.familie.oppdrag.iverksetting

import io.mockk.*
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.oppdrag.repository.OppdragLager
import no.nav.familie.oppdrag.repository.OppdragRepository
import no.nav.familie.oppdrag.repository.somOppdragLager
import no.nav.familie.oppdrag.repository.somOppdragLagerMedVersjon
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer
import org.assertj.core.api.Assertions
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
        val oppdragLagerRepository = mockk<OppdragRepository>()
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

        val oppdragLagerRepository = mockk<OppdragRepository>()

        every { oppdragLagerRepository.hentAlleVersjonerAvOppdrag(any(), any(), any()) } returns
                listOf(oppdragLager)

        val slot = slot<OppdragLager>()
        every { oppdragLagerRepository.update(capture(slot)) } returns mockk()

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragLagerRepository.hentAlleVersjonerAvOppdrag(any(), any(), any()) }
        verify(exactly = 1) { oppdragLagerRepository.update(slot.captured) }
        Assertions.assertThat(slot.captured.kvitteringsmelding).isNotNull
        Assertions.assertThat(slot.captured.status).isEqualTo(OppdragStatus.KVITTERT_OK)

    }

    @Test
    fun skal_lagre_kvittering_på_riktig_versjon() {
        val oppdragLager = utbetalingsoppdragMedTilfeldigAktoer().somOppdragLager.apply { status = OppdragStatus.KVITTERT_OK }
        val oppdragLagerV1 = utbetalingsoppdragMedTilfeldigAktoer().somOppdragLagerMedVersjon(1)

        val oppdragLagerRepository = mockk<OppdragRepository>()

        every { oppdragLagerRepository.hentAlleVersjonerAvOppdrag(any(), any(), any()) } returns
                listOf(oppdragLager, oppdragLagerV1)

        val slot = slot<OppdragLager>()
        every { oppdragLagerRepository.update(capture(slot)) } returns mockk()

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        Assertions.assertThat(slot.captured.kvitteringsmelding).isNotNull
        Assertions.assertThat(slot.captured.versjon).isEqualTo(1)
        verify(exactly = 1) { oppdragLagerRepository.update(slot.captured) }
    }


    @Test
    fun skal_logge_error_hvis_det_finnes_to_identiske_oppdrag_i_databasen() {

        val oppdragLagerRepository = mockk<OppdragRepository>()

        every { oppdragLagerRepository.hentAlleVersjonerAvOppdrag(any(), any(), any()) } throws Exception()

        every { oppdragLagerRepository.insert(any()) } returns mockk()

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        assertThrows<Exception> { oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage) }
        verify(exactly = 0) { oppdragLagerRepository.insert(any<OppdragLager>()) }
    }

    @Test
    fun skal_logge_error_hvis_oppdraget_mangler_i_databasen() {
        val oppdragLagerRepository = mockk<OppdragRepository>()

        every { oppdragLagerRepository.hentAlleVersjonerAvOppdrag(any(), any(), any()) } throws Exception()
        every { oppdragLagerRepository.insert(any()) } returns mockk()

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.error(any()) } just Runs

        assertThrows<Exception> { oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage) }
        verify(exactly = 0) { oppdragLagerRepository.insert(any<OppdragLager>()) }
    }

    @Test
    fun skal_logge_warn_hvis_oppdrag_i_databasen_har_uventet_status() {
        val oppdragLager = utbetalingsoppdragMedTilfeldigAktoer().somOppdragLager

        val oppdragLagerRepository = mockk<OppdragRepository>()

        every { oppdragLagerRepository.hentAlleVersjonerAvOppdrag(any(), any(), any()) } returns
                listOf(oppdragLager.copy(status = OppdragStatus.KVITTERT_OK))

        every { oppdragLagerRepository.update(any()) } returns mockk()

        val oppdragMottaker = OppdragMottaker(oppdragLagerRepository, devEnv)
        oppdragMottaker.LOG = mockk()

        every { oppdragMottaker.LOG.info(any()) } just Runs
        every { oppdragMottaker.LOG.warn(any()) } just Runs
        every { oppdragMottaker.LOG.debug(any()) } just Runs

        oppdragMottaker.mottaKvitteringFraOppdrag("kvittering-akseptert.xml".fraRessursSomTextMessage)

        verify(exactly = 1) { oppdragLagerRepository.hentAlleVersjonerAvOppdrag(any(), any(), any()) }
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
