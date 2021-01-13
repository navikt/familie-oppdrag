package no.nav.familie.oppdrag.simulering

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.simulering.*
import no.nav.sbl.util.StringUtils
import no.nav.system.os.entiteter.beregningskjema.Beregning
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaa
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaaDetaljer
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SimuleringResultatTransformer {

    fun mapSimulering(beregning: Beregning, utbetalingsoppdrag: Utbetalingsoppdrag): DetaljertSimuleringResultat {
        val mottakerMap = hashMapOf<String, MutableList<SimulertPostering>>()
        for (periode in beregning.beregningsPeriode) {
            for (stoppnivaa in periode.beregningStoppnivaa) {
                val posteringer = arrayListOf<SimulertPostering>()
                for (detaljer in stoppnivaa.beregningStoppnivaaDetaljer) {
                    val postering: SimulertPostering = mapPostering(false, stoppnivaa, detaljer)
                    posteringer.add(postering)
                }
                val utbetalesTilId = stoppnivaa.utbetalesTilId
                val posteringListe = mottakerMap.getOrPut(utbetalesTilId) { mutableListOf() }
                posteringListe.addAll(posteringer)
            }
        }

        val requestMottakerId = hentOrgNrEllerFnr(utbetalingsoppdrag.aktoer)
        val simuleringMottakerListe = mottakerMap.map { (utbetalesTilId, simulertPostering) ->
            SimuleringMottaker(mottakerNummer = utbetalesTilId,
                               simulertPostering = simulertPostering,
                               mottakerType = utledMottakerType(utbetalesTilId, hentOrgNrEllerFnr(utbetalesTilId) == requestMottakerId))
        }
        return DetaljertSimuleringResultat(simuleringMottakerListe)
    }

    private fun mapPostering(utenInntrekk: Boolean,
                             stoppnivaa: BeregningStoppnivaa,
                             detaljer: BeregningStoppnivaaDetaljer): SimulertPostering {
        return SimulertPostering(
                betalingType = utledBetalingType(detaljer.belop),
                beløp = detaljer.belop,
                fagOmrådeKode = FagOmrådeKode.fraKode(stoppnivaa.kodeFagomraade),
                fom = parseDato(detaljer.faktiskFom),
                tom = parseDato(detaljer.faktiskTom),
                forfallsdato = parseDato(stoppnivaa.forfall),
                posteringType = PosteringType.fraKode(detaljer.typeKlasse),
                utenInntrekk = utenInntrekk)
    }

    private fun hentOrgNrEllerFnr(orgNrEllerFnr: String): String {
        return if (erOrgNr(orgNrEllerFnr)) {
            orgNrEllerFnr.substring(2)
        } else {
            orgNrEllerFnr
        }
    }

    private fun utledMottakerType(utbetalesTilId: String, harSammeAktørIdSomBruker: Boolean): MottakerType {
        if (harSammeAktørIdSomBruker) {
            return MottakerType.BRUKER
        }
        return if (erOrgNr(utbetalesTilId)) {
            MottakerType.ARBG_ORG
        } else MottakerType.ARBG_PRIV
    }

    private fun erOrgNr(verdi: String): Boolean {
        if (StringUtils.nullOrEmpty(verdi)) {
            throw IllegalArgumentException("org.nr verdi er tom")
        }
        // orgNr i responsen fra økonomi starter med "00"
        return "00" == verdi.substring(0, 2)
    }

    private fun utledBetalingType(belop: BigDecimal): BetalingType {
        return if (belop > BigDecimal.ZERO) {
            BetalingType.DEBIT
        } else BetalingType.KREDIT
    }

    private fun parseDato(dato: String): LocalDate {
        val dtf = DateTimeFormatter.ofPattern(DATO_PATTERN)
        return LocalDate.parse(dato, dtf)
    }

    companion object {

        private const val DATO_PATTERN = "yyyy-MM-dd"
    }
}
