package no.nav.familie.oppdrag.simulering

import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.oppdrag.common.deepClone
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningFeilUnderBehandling
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerFpService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Profile("!dev & !dev_psql_mq")
@Service
class SimuleringSenderImpl(
    private val port: SimulerFpService,
) : SimuleringSender {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Override
    @Retryable(value = [SimulerBeregningFeilUnderBehandling::class], maxAttempts = 3, backoff = Backoff(delay = 4000))
    override fun hentSimulerBeregningResponse(
        simulerBeregningRequest: SimulerBeregningRequest?,
    ): Pair<SimulerBeregningResponse, SimulerBeregningResponse?> {
        val responsForAlleFagsaker = port.simulerBeregning(simulerBeregningRequest)

        logger.info("Respons for alle fagsaker: ${objectMapper.writeValueAsString(responsForAlleFagsaker)}")

        val requestFagsystemId = simulerBeregningRequest?.request?.oppdrag?.fagsystemId

        return if (requestFagsystemId != null && responsForAlleFagsaker.response?.simulering != null) {
            val (responsForFagsak, responsForAndreFagsaker) = splitResponsePåFagsakId(
                responsForAlleFagsaker,
                requestFagsystemId
            )
            responsForFagsak to responsForAndreFagsaker

        } else {
            responsForAlleFagsaker to null
        }
    }
}

fun splitResponsePåFagsakId(
    response: SimulerBeregningResponse,
    fagsakId: String
): Pair<SimulerBeregningResponse, SimulerBeregningResponse> {
    val responsForFagsak = response.deepClone()
    responsForFagsak.response.simulering.beregningsPeriode.forEach { beregningsPeriode ->
        beregningsPeriode.beregningStoppnivaa.removeIf { stoppnivaa ->
            stoppnivaa.fagsystemId.trim() != fagsakId
        }
    }

    val responsForAndreFagsaker = response.deepClone()
    responsForAndreFagsaker.response.simulering.beregningsPeriode.forEach { beregningsPeriode ->
        beregningsPeriode.beregningStoppnivaa.removeIf { stoppnivaa ->
            stoppnivaa.fagsystemId.trim() == fagsakId
        }
    }

    return responsForFagsak to responsForAndreFagsaker
}
