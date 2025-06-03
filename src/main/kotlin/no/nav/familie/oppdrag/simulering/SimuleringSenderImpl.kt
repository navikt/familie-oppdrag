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
class SimuleringSenderImpl(
    private val port: SimulerFpService,
) : SimuleringSender {
    @Override
    @Retryable(value = [SimulerBeregningFeilUnderBehandling::class], maxAttempts = 3, backoff = Backoff(delay = 4000))
    override fun hentSimulerBeregningResponse(simulerBeregningRequest: SimulerBeregningRequest?): SimulerBeregningResponse {
        val response = port.simulerBeregning(simulerBeregningRequest)

        // Filter out beregningStoppnivaa with different fagsystemId than the one in the request
        val requestFagsystemId = simulerBeregningRequest?.request?.oppdrag?.fagsystemId

        if (requestFagsystemId != null && response.response?.simulering != null) {
            response.response.simulering.beregningsPeriode.forEach { beregningsPeriode ->
                beregningsPeriode.beregningStoppnivaa.removeIf { stoppnivaa ->
                    stoppnivaa.fagsystemId.trim() != requestFagsystemId
                }
            }
        }

        return response
    }
}
