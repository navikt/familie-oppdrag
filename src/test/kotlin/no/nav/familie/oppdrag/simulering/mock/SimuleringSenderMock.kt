package no.nav.familie.oppdrag.simulering.mock

import no.nav.familie.oppdrag.simulering.SimuleringSender
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("dev", "dev_psql_mq")
@Service
class SimuleringSenderMock : SimuleringSender {
    override fun hentSimulerBeregningResponse(simulerBeregningRequest: SimulerBeregningRequest?): SimulerBeregningResponse {
        return SimuleringGenerator().opprettSimuleringsResultat(simulerBeregningRequest!!)
    }
}
