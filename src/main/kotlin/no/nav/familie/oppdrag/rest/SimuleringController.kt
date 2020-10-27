package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.RestSimulerResultat
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.common.RessursUtils.ok
import no.nav.familie.oppdrag.simulering.SimuleringTjeneste
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/simulering")
@ProtectedWithClaims(issuer = "azuread")
class SimuleringController(@Autowired val simuleringTjeneste: SimuleringTjeneste) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/etterbetalingsbelop"])
    fun hentEtterbetalingsbeløp(@Valid @RequestBody
                                utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<RestSimulerResultat>> {
        LOG.info("Hente simulert etterbetaling for saksnr ${utbetalingsoppdrag.saksnummer}")
        return ok(simuleringTjeneste.utførSimulering(utbetalingsoppdrag))
    }

    //Temporær funksjon som skal brukes for å teste responser fra oppdrag.
    //TODO: skal fjernes når den ikke mer er i bruk.
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/direktesimulering"])
    fun direkteSimulering(@Valid @RequestBody
                          utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<SimulerBeregningResponse>> =
            ok(simuleringTjeneste.hentSimulerBeregningResponse(utbetalingsoppdrag))

    companion object {

        val LOG = LoggerFactory.getLogger(SimuleringController::class.java)
    }
}