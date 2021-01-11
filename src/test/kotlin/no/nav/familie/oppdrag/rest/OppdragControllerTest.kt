package no.nav.familie.oppdrag.rest

import io.mockk.*
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Opphør
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import no.nav.familie.oppdrag.repository.OppdragLager
import no.nav.familie.oppdrag.repository.OppdragRepository
import no.nav.familie.oppdrag.service.OppdragServiceImpl
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

internal class OppdragControllerTest {

    val localDateTimeNow = LocalDateTime.now()
    val localDateNow = LocalDate.now()

    val utbetalingsoppdrag = Utbetalingsoppdrag(
            Utbetalingsoppdrag.KodeEndring.NY,
            "BA",
            "SAKSNR",
            "PERSONID",
            "SAKSBEHANDLERID",
            localDateTimeNow,
            listOf(Utbetalingsperiode(true,
                    Opphør(localDateNow),
                    2,
                    1,
                    localDateNow,
                    "BATR",
                    localDateNow,
                    localDateNow,
                    BigDecimal.ONE,
                    Utbetalingsperiode.SatsType.MND,
                    "UTEBETALES_TIL",
                    1))
    )

    @Test
    fun skal_lagre_oppdrag_for_utbetalingoppdrag() {

        val mapper = OppdragMapper()
        val oppdragSender = mockk<OppdragSender>(relaxed = true)

        val oppdragLagerRepository = mockk<OppdragRepository>()
        every { oppdragLagerRepository.insert(any()) } returns mockk()

        val oppdragService = OppdragServiceImpl(oppdragSender, oppdragLagerRepository)

        val oppdragController = OppdragController(oppdragService, mapper)

        oppdragController.sendOppdrag(utbetalingsoppdrag)

        verify {
            oppdragLagerRepository.insert(match<OppdragLager> {
                it.utgåendeOppdrag.contains("BA")
                        && it.status == OppdragStatus.LAGT_PÅ_KØ
                        && it.opprettetTidspunkt > localDateTimeNow
            })
        }
    }
}