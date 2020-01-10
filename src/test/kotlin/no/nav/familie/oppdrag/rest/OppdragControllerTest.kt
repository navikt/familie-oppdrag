package no.nav.familie.oppdrag.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.kontrakter.felles.oppdrag.Opphør
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.familie.oppdrag.repository.OppdragProtokollStatus
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

internal class OppdragControllerTest{

    val localDateTimeNow = LocalDateTime.now()
    val localDateNow = LocalDate.now()

    val utbetalingsoppdrag = Utbetalingsoppdrag(
            Utbetalingsoppdrag.KodeEndring.NY,
            "FAGSYSTEM_TEST",
            "SAKSNR",
            "PERSONID",
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

    @Test
    fun skal_lagre_oppdragprotokoll_for_utbetalingoppdrag() {

        val mapper = OppdragMapper()
        val oppdragSender = mockk<OppdragSender>(relaxed = true)

        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()
        every { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) } answers { emptyList() }
        every { oppdragProtokollRepository.lagreOppdrag(any<OppdragProtokoll>()) } answers { 0 }

        val oppdragController = OppdragController(oppdragSender, mapper, oppdragProtokollRepository)

        oppdragController.sendOppdrag(utbetalingsoppdrag)

        verify {
            oppdragProtokollRepository.lagreOppdrag(match<OppdragProtokoll> {
                it.melding.contains("FAGSYSTEM_TEST")
                && it.status == OppdragProtokollStatus.LAGT_PÅ_KØ
                && it.opprettetTidspunkt > localDateTimeNow
            })
        }
    }

    @Test
    fun skal_ikke_lagre_oppdragsprotokoll_for_eksisterende_oppdrag() {

        val mapper = OppdragMapper()
        val oppdragSender = mockk<OppdragSender>(relaxed = true)
        val oppdragProtokollRepository = mockk<OppdragProtokollRepository>()
        val testOppdragsProtokoll = OppdragProtokoll(
                "PERSONID",
                "FAGSYSTEM_TEST",
                "SAKSNR",
                "1",
                "INPUT_DATA",
                "MELDING",
                OppdragProtokollStatus.LAGT_PÅ_KØ,
                localDateTimeNow,
                localDateTimeNow)
        val oppdragController = OppdragController(oppdragSender, mapper, oppdragProtokollRepository)

        every { oppdragProtokollRepository.hentOppdrag(any(), any(), any()) } answers { listOf(testOppdragsProtokoll) }

         val svar = oppdragController.sendOppdrag(utbetalingsoppdrag)

        verify (exactly = 0) { oppdragProtokollRepository.lagreOppdrag(any<OppdragProtokoll>()) }
        assertEquals(HttpStatus.BAD_REQUEST, svar.statusCode)
    }
}