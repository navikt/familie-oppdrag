package no.nav.familie.oppdrag.simulering

import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaa
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse

interface SimuleringSender {
    fun hentSimulerBeregningResponse(
        simulerBeregningRequest: SimulerBeregningRequest?,
    ): SimulerBeregningResponse
}
