package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.RestSimulerResultat
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.FeilutbetalingerFraSimulering
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.oppdrag.common.RessursUtils.ok
import no.nav.familie.oppdrag.simulering.SimuleringTjeneste
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/simulering", produces = [MediaType.APPLICATION_JSON_VALUE])
@ProtectedWithClaims(issuer = "azuread")
class SimuleringController(@Autowired val simuleringTjeneste: SimuleringTjeneste) {

    val logger: Logger = LoggerFactory.getLogger(SimuleringController::class.java)


    @PostMapping(path = ["/etterbetalingsbelop"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun hentEtterbetalingsbeløp(@Valid @RequestBody
                                utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<RestSimulerResultat>> {
        logger.info("Hente simulert etterbetaling for saksnr ${utbetalingsoppdrag.saksnummer}")
        return ok(simuleringTjeneste.utførSimulering(utbetalingsoppdrag))
    }

    @PostMapping(path = ["/v1"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun utførSimuleringOgHentResultat(@Valid @RequestBody utbetalingsoppdrag: Utbetalingsoppdrag)
            : ResponseEntity<Ressurs<DetaljertSimuleringResultat>> {
        return ok(simuleringTjeneste.utførSimuleringOghentDetaljertSimuleringResultat(utbetalingsoppdrag))
    }

    //Temporær funksjon som skal brukes for å teste responser fra oppdrag.
    //TODO: skal fjernes når den ikke mer er i bruk.
    @PostMapping(path = ["/direktesimulering"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun direkteSimulering(@Valid @RequestBody
                          utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<SimulerBeregningResponse>> =
            ok(simuleringTjeneste.hentSimulerBeregningResponse(utbetalingsoppdrag))


    @GetMapping(path = ["/feilutbetalinger/{ytelsestype}/{eksternFagsakId}/{eksternBehandlingId}"])
    fun hentFeilutbetalinger(@PathVariable ytelsestype: Ytelsestype,
                             @PathVariable eksternFagsakId: String,
                             @PathVariable eksternBehandlingId: String)
            : ResponseEntity<Ressurs<FeilutbetalingerFraSimulering>> {
        logger.info("Henter feilutbetalinger for ytelsestype=${ytelsestype}, " +
                    "fagsak=${eksternFagsakId}, " +
                    "behandlingId=${eksternBehandlingId}")
        return ok(simuleringTjeneste.hentFeilutbetalinger(ytelsestype, eksternFagsakId, eksternBehandlingId))
    }
}
