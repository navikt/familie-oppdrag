package no.nav.familie.oppdrag.simulering

import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import org.springframework.context.annotation.Profile
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Profile("!dev")
@Service
class SimuleringSenderImpl(private val port: SimulerFpService) : SimuleringSender {

    @Override
    @Retryable(value = [SimulerBeregningFeilUnderBehandling::class], maxAttempts = 3, backoff = Backoff(delay = 4000))
    override fun hentSimulerBeregningResponse(simulerBeregningRequest: SimulerBeregningRequest?): SimulerBeregningResponse {
        return port.simulerBeregning(simulerBeregningRequest)
    }
}
