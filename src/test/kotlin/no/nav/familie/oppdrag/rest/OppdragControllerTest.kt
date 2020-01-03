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
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

internal class OppdragControllerTest{

    val localDateTimeNow = LocalDateTime.now()
    val localDateNow = LocalDate.now()

    val utbetalingsoppdrag = Utbetalingsoppdrag(
            Utbetalingsoppdrag.KodeEndring.NY,
            "FAGSYSTEM_TEST",
            "SAKSNR",
            "AKTØRID",
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
        every { oppdragProtokollRepository.save(any<OppdragProtokoll>()) } answers { arg(0) }

        val oppdragController = OppdragController(oppdragSender,mapper,oppdragProtokollRepository)

        oppdragController.sendOppdrag(utbetalingsoppdrag)

        verify {
            oppdragProtokollRepository.save(match<OppdragProtokoll> {
                it.melding.contains("FAGSYSTEM_TEST")
                && it.status == OppdragProtokollStatus.LAGT_PÅ_KØ
                && it.opprettetTidspunkt > localDateTimeNow
                && it.serienummer == 0L
            })
        }
    }
}