package no.nav.familie.oppdrag.simulering

import no.nav.familie.kontrakter.felles.oppdrag.RestSimulerResultat
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse

interface SimuleringTjeneste {

    fun utførSimulering(utbetalingsoppdrag: Utbetalingsoppdrag): RestSimulerResultat
    fun hentSimulerBeregningResponse(utbetalingsoppdrag: Utbetalingsoppdrag): SimulerBeregningResponse
}
