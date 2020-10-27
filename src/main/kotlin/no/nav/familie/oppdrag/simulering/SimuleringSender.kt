package no.nav.familie.oppdrag.simulering

import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class SimuleringSender(private val port: SimulerFpService) {

    @Retryable(value = [SimulerBeregningFeilUnderBehandling::class], maxAttempts = 3, backoff = Backoff(delay = 4000))
    fun hentSimulerBeregningResponse(simulerBeregningRequest: SimulerBeregningRequest?): SimulerBeregningResponse =
            port.simulerBeregning(simulerBeregningRequest)
}