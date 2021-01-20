package no.nav.familie.oppdrag.simulering

import no.nav.familie.kontrakter.felles.oppdrag.RestSimulerResultat
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun SimulerBeregningResponse.toRestSimulerResult(
        dato: LocalDate = LocalDate.now()): RestSimulerResultat {

    val totalEtterbetalingsBeløp =
            this.response?.simulering?.beregningsPeriode?.sumBy { finnEtterbetalingPerPeriode(it, dato) }
            ?: 0

    return RestSimulerResultat(etterbetaling = totalEtterbetalingsBeløp)
}

private fun finnEtterbetalingPerPeriode(beregningsPeriode: BeregningsPeriode, dato: LocalDate, kodeFagomraade: String = "BA"): Int {
    // Fremtidige perioder gir ingen etterbetaling.
    val datoFraPeriode = LocalDate.parse(beregningsPeriode.periodeFom, DateTimeFormatter.ISO_DATE)
    if (datoFraPeriode > dato) return 0

    val stoppNivaBA =
            beregningsPeriode.beregningStoppnivaa.filter { it.kodeFagomraade?.trim() == kodeFagomraade }

    // Feilutbetaling medfører at etterbetaling er 0
    val inneholderFeilutbetalingType =
            stoppNivaBA.any { stopNivå -> stopNivå.beregningStoppnivaaDetaljer.any { detaljer -> detaljer.typeKlasse?.trim() == TypeKlasse.FEIL.name } }
    if (inneholderFeilutbetalingType) return 0

    // Summer perioder av type YTEL og med forfallsdato bak i tiden.
    val sum = stoppNivaBA.filter { forfallPassert(it.forfall, dato) }
            .flatMap { it.beregningStoppnivaaDetaljer }
            .filter { it.typeKlasse?.trim() == TypeKlasse.YTEL.name }
            .sumBy { it.belop?.toInt() ?: 0 }
    return sum
}

private fun forfallPassert(forfall: String, dato: LocalDate): Boolean =
        dato >= LocalDate.parse(forfall, DateTimeFormatter.ISO_DATE)

// TODO: Er dette det same som PosteringType?
enum class TypeKlasse {
    FEIL,
    YTEL
}
