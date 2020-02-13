package no.nav.familie.oppdrag.util

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

object TestOppdragMedAvstemmingsdato {

    private val FAGSAKID = "123456789"
    private val AKTOER = "12345678911"
    private val SATS = BigDecimal.valueOf(1054)

    fun lagTestUtbetalingsoppdrag(avstemmingstidspunkt : LocalDateTime, fagområde: String) = Utbetalingsoppdrag(
            Utbetalingsoppdrag.KodeEndring.NY,
            fagområde,
            FAGSAKID,
            AKTOER,
            "Z999999",
            avstemmingstidspunkt,
            listOf(Utbetalingsperiode(false,
                    null,
                    1,
                    null,
                    LocalDate.now(),
                    if (fagområde.equals("BA")) "BATR" else "EF",
                    LocalDate.now().withDayOfMonth(1),
                    LocalDate.now().plusYears(6),
                    SATS,
                    Utbetalingsperiode.SatsType.MND,
                    AKTOER,
                    Random.nextLong()))
    )
}