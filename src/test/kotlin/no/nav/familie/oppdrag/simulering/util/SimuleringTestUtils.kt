package no.nav.familie.oppdrag.simulering.util

import io.mockk.InternalPlatformDsl.toStr
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.simulering.TypeKlasse
import no.nav.system.os.entiteter.beregningskjema.Beregning
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaa
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaaDetaljer
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.*

fun lagTestUtbetalingsoppdragForFGBMedEttBarn()
        : Utbetalingsoppdrag {

    val personIdent = "12345678901"

    val vedtakDato = LocalDate.now()
    val datoFom: LocalDate = YearMonth.now().minusMonths(1).atDay(1)
    val datoTom: LocalDate = YearMonth.now().plusMonths(3).atEndOfMonth()
    val fagsakId = "5566"
    val behandlingId = 334455L

    return Utbetalingsoppdrag(
            Utbetalingsoppdrag.KodeEndring.NY,
            "BA",
            fagsakId,
            UUID.randomUUID().toString(),
            "SAKSBEHANDLERID",
            LocalDateTime.now(),
            listOf(Utbetalingsperiode(false,
                                      null,
                                      1,
                                      null,
                                      vedtakDato,
                                      "BATR",
                                      datoFom,
                                      datoTom,
                                      BigDecimal(1054),
                                      Utbetalingsperiode.SatsType.MND,
                                      personIdent,
                                      behandlingId
            ))
    )
}

fun lagBeregningsPeriode(bergeningStopNiva: List<BeregningStoppnivaa>, date: LocalDate): BeregningsPeriode {

    val beregningsPeriode = BeregningsPeriode()
    beregningsPeriode.periodeFom = date.with(TemporalAdjusters.firstDayOfMonth()).toString()
    beregningsPeriode.periodeTom = date.with(TemporalAdjusters.lastDayOfMonth()).toString()
    beregningsPeriode.beregningStoppnivaa.addAll(bergeningStopNiva)

    return beregningsPeriode
}

fun lagSimulerBeregningResponse(beregningsPerioder: List<BeregningsPeriode>): SimulerBeregningResponse {

    val beregning = Beregning()
    beregning.beregningsPeriode.addAll(beregningsPerioder)

    val simulerBeregningResponse =
            no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningResponse()
    simulerBeregningResponse.simulering = beregning

    val response = SimulerBeregningResponse()
    response.response = simulerBeregningResponse

    return response
}

fun lagTestSimuleringResponse(): SimulerBeregningResponse {
    val currentDate: LocalDate = LocalDate.now()

    val enTideligereMåned = currentDate.plusMonths(1)

    val periodeNåværendeMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppniva(currentDate)), currentDate)

    val periodeTidligereMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppniva(enTideligereMåned, 2)), enTideligereMåned)

    return lagSimulerBeregningResponse(listOf(periodeNåværendeMåned, periodeTidligereMåned))
}

fun lagBeregningStoppnivaFeilUtbetaling(date: LocalDate,
                                        forfall: Long = 0,
                                        fagOmrade: String = "BATR"): BeregningStoppnivaa {
    val beregningStoppnivaa = BeregningStoppnivaa()
    beregningStoppnivaa.forfall = date.plusDays(forfall).toString()
    beregningStoppnivaa.kodeFagomraade = fagOmrade

    lagBeregningStoppnivaaDetaljer(TypeKlasse.FEIL.name)

    return beregningStoppnivaa
}

fun lagBeregningStoppniva(date: LocalDate,
                          forfall: Long = 0,
                          fagOmrade: String = "BATR"): BeregningStoppnivaa {

    val beregningStoppnivaa = BeregningStoppnivaa()
    beregningStoppnivaa.forfall = date.plusDays(forfall).toString()
    beregningStoppnivaa.kodeFagomraade = fagOmrade

    beregningStoppnivaa.beregningStoppnivaaDetaljer.add(lagBeregningStoppnivaaDetaljer(dato = date))
    beregningStoppnivaa.utbetalesTilId = "1234567890"

    return beregningStoppnivaa
}

fun lagBeregningStoppnivaRevurdering(date: LocalDate,
                                     forfall: Long = 0,
                                     fagOmrade: String = "BATR"): BeregningStoppnivaa {
    val beregningStoppnivaa = BeregningStoppnivaa()
    beregningStoppnivaa.forfall = date.plusDays(forfall).toString()
    beregningStoppnivaa.kodeFagomraade = fagOmrade

    beregningStoppnivaa.beregningStoppnivaaDetaljer.add(lagBeregningStoppnivaaDetaljer(belop = BigDecimal(1000)))
    beregningStoppnivaa.beregningStoppnivaaDetaljer.add(lagBeregningStoppnivaaDetaljer(belop = BigDecimal(-500)))

    return beregningStoppnivaa
}

private fun lagBeregningStoppnivaaDetaljer(typeKlasse: String = TypeKlasse.YTEL.name,
                                           belop: BigDecimal = BigDecimal(1000),
                                           dato: LocalDate? = null): BeregningStoppnivaaDetaljer {
    val beregningStoppnivaaDetaljer = BeregningStoppnivaaDetaljer()
    beregningStoppnivaaDetaljer.typeKlasse = typeKlasse
    beregningStoppnivaaDetaljer.belop = belop
    beregningStoppnivaaDetaljer.faktiskFom = dato?.minusMonths(2).toStr()
    beregningStoppnivaaDetaljer.faktiskTom = dato?.toStr()
    return beregningStoppnivaaDetaljer
}
