package no.nav.familie.oppdrag.simulering

import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningFeilUnderBehandling
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerFpService
import org.springframework.context.annotation.Profile
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Profile("!dev & !dev_psql_mq")
@Service
class SimuleringSenderImpl(private val port: SimulerFpService) : SimuleringSender {
    @Override
    @Retryable(value = [SimulerBeregningFeilUnderBehandling::class], maxAttempts = 3, backoff = Backoff(delay = 4000))
    override fun hentSimulerBeregningResponse(simulerBeregningRequest: SimulerBeregningRequest?): SimulerBeregningResponse {
        return port.simulerBeregning(simulerBeregningRequest)
    }
}
