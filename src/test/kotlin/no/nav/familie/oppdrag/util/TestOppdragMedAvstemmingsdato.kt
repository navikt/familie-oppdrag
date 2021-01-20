package no.nav.familie.oppdrag.util

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

object TestOppdragMedAvstemmingsdato {

    private val FAGSAKID = "123456789"
    private val AKTOER = "12345678911"

    fun lagTestUtbetalingsoppdrag(
            avstemmingstidspunkt: LocalDateTime, fagområde: String,
            fagsak: String = FAGSAKID,
            vararg utbetalingsperiode: Utbetalingsperiode = arrayOf(lagUtbetalingsperiode(fagområde)),
    ) =
            Utbetalingsoppdrag(
                    Utbetalingsoppdrag.KodeEndring.NY,
                    fagområde,
                    fagsak,
                    AKTOER,
                    "Z999999",
                    avstemmingstidspunkt,
                    utbetalingsperiode.toList()
            )

    fun lagUtbetalingsperiode(
            fagområde: String = "BA",
            periodeId: Long = 1,
            beløp: Int = 100,
            fom: LocalDate = LocalDate.now().withDayOfMonth(1),
            tom: LocalDate = LocalDate.now().plusYears(6),
    ) =
            Utbetalingsperiode(false,
                               null,
                               periodeId,
                               null,
                               LocalDate.now(),
                               if (fagområde.equals("BA")) "BATR" else "EF",
                               fom,
                               tom,
                               beløp.toBigDecimal(),
                               Utbetalingsperiode.SatsType.MND,
                               AKTOER,
                               Random.nextLong())

}