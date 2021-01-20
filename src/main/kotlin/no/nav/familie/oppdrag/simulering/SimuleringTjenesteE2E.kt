package no.nav.familie.oppdrag.simulering

import no.nav.familie.kontrakter.felles.oppdrag.RestSimulerResultat
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope

@Service
@ApplicationScope
@Profile("e2e")
class SimuleringTjenesteE2E(): SimuleringTjeneste {

    override fun utførSimulering(utbetalingsoppdrag: Utbetalingsoppdrag): RestSimulerResultat = RestSimulerResultat(0)
    override fun utførSimuleringOghentDetaljertSimuleringResultat(utbetalingsoppdrag: Utbetalingsoppdrag): DetaljertSimuleringResultat = DetaljertSimuleringResultat(simuleringMottaker = emptyList())
    override fun hentSimulerBeregningResponse(utbetalingsoppdrag: Utbetalingsoppdrag): SimulerBeregningResponse = SimulerBeregningResponse()
}
