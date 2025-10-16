package no.nav.familie.oppdrag.simulering

import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaa
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse

/**
 * Fjerner data som gjelder andre fagsaker enn den vi jobber med, i tillegg til et map med dataen som er fjernet
 * (Dette er en workaround siden vi ikke kan bruke .copy() på responsen)
 *
 * @return SimulerBeregningResponse uten perioder som gjelder andre fagsaker og et map med beregningsPerioder til
 * BeregningStoppnivaa som gjelder KUN andre fagsaker
 */
fun splitResponsePåFagsakId(
    response: SimulerBeregningResponse,
    fagsakId: String,
): Pair<SimulerBeregningResponse, Map<BeregningsPeriode, List<BeregningStoppnivaa>>> {
    val beregninsPerioderForAndreFagsaker = mutableMapOf<BeregningsPeriode, List<BeregningStoppnivaa>>()
    response.response.simulering.beregningsPeriode.forEach { beregningsPeriode ->
        beregninsPerioderForAndreFagsaker[beregningsPeriode] =
            beregningsPeriode.beregningStoppnivaa.filter { it.fagsystemId != fagsakId }
    }

    response.response.simulering.beregningsPeriode.forEach { beregningsPeriode ->
        beregningsPeriode.beregningStoppnivaa.removeIf { it.fagsystemId != fagsakId }
    }

    return response to beregninsPerioderForAndreFagsaker
}

/**
 * Bytter ut data i responsen med data oppgitt i beregningStoppnivaa.
 * Brukes for å endre responsen slik at den kun inneholder data fra andre fagsaker enn @param fagsystemId
 */
fun byttUtBeregningStoppnivaa(
    respons: SimulerBeregningResponse,
    beregningStoppnivaa: Map<BeregningsPeriode, List<BeregningStoppnivaa>>,
    fagsystemId: String,
) {
    respons.response?.simulering?.beregningsPeriode?.forEach { beregningPeriode ->
        beregningPeriode.beregningStoppnivaa.addAll(beregningStoppnivaa[beregningPeriode] ?: emptyList())
        beregningPeriode.beregningStoppnivaa.removeIf { it.fagsystemId == fagsystemId }
        beregningPeriode.beregningStoppnivaa.distinct()
    }
}
