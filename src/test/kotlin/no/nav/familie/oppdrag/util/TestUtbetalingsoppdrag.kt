package no.nav.familie.oppdrag.util

import no.nav.familie.kontrakter.felles.oppdrag.Opphør
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

object TestUtbetalingsoppdrag {

    private val localDateTimeNow = LocalDateTime.now()
    private val localDateNow = LocalDate.now()

    fun utbetalingsoppdragMedTilfeldigAktoer() = Utbetalingsoppdrag(
            Utbetalingsoppdrag.KodeEndring.NY,
            "BA",
            "SAKSNR",
            UUID.randomUUID().toString(), // Foreløpig plass til en 50-tegn string og ingen gyldighetssjekk
            "SAKSBEHANDLERID",
            localDateTimeNow,
            listOf(Utbetalingsperiode(false,
                    Opphør(localDateNow),
                    1,
                    null,
                    localDateNow,
                    "KLASSE A",
                    localDateNow,
                    localDateNow,
                    BigDecimal.ONE,
                    Utbetalingsperiode.SatsType.MND,
                    "UTEBETALES_TIL",
                    1))
    )

}