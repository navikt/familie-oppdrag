package no.nav.familie.oppdrag.simulering

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope

@Service
@ApplicationScope
class SimuleringTjeneste(@Autowired val simuleringSender: SimuleringSender,
                         @Autowired val simulerBeregningRequestMapper: SimulerBeregningRequestMapper,
                         @Autowired val simulerBeregningResponseMapper: SimulerBeregningResponseMapper) {

    fun utførSimulering(utbetalingsoppdrag: Utbetalingsoppdrag): SimulerResultatDto {
        val simulerBeregningRequest = simulerBeregningRequestMapper.tilSimulerBeregningRequest(utbetalingsoppdrag)

        return simulerBeregningResponseMapper.toSimulerResultDto(
                simuleringSender.hentSimulerBeregningResponse(simulerBeregningRequest)
        )
    }
}