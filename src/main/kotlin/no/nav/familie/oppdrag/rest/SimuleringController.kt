package no.nav.familie.oppdrag.rest

import jakarta.validation.Valid
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.FeilutbetalingerFraSimulering
import no.nav.familie.kontrakter.felles.simulering.HentFeilutbetalingerFraSimuleringRequest
import no.nav.familie.oppdrag.common.RessursUtils.ok
import no.nav.familie.oppdrag.common.RessursUtils.serviceUnavailable
import no.nav.familie.oppdrag.featuretoggle.FeatureToggle
import no.nav.familie.oppdrag.featuretoggle.FeatureToggleService
import no.nav.familie.oppdrag.simulering.SimuleringTjeneste
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    "/api/simulering",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class SimuleringController(
    @Autowired val simuleringTjeneste: SimuleringTjeneste,
    private val featureToggleService: FeatureToggleService,
) {
    val logger: Logger = LoggerFactory.getLogger(SimuleringController::class.java)

    @PostMapping(path = ["/v1"])
    fun utførSimuleringOgHentResultat(
        @Valid @RequestBody
        utbetalingsoppdrag: Utbetalingsoppdrag,
    ): ResponseEntity<Ressurs<DetaljertSimuleringResultat>> {
        if (featureToggleService.isEnabled(FeatureToggle.SKRU_AV_SIMULERING, true)) {
            logger.info(
                "Fagsystem: ${utbetalingsoppdrag.fagSystem} forsøker å simulere, men simulering er skrudd av for familie-oppdrag. Simulering skal nå gjøres via familie-oppdrag-backend i GCP (http://familie-oppdrag-backend).",
            )
            return serviceUnavailable(
                "Simulering er skrudd av for familie-oppdrag. All fremtidig simulering skal gjøres via familie-oppdrag-backend i GCP (http://familie-oppdrag-backend).",
            )
        }
        return ok(simuleringTjeneste.utførSimuleringOghentDetaljertSimuleringResultat(utbetalingsoppdrag))
    }

    @PostMapping(path = ["/feilutbetalinger"])
    fun hentFeilutbetalinger(
        @Valid @RequestBody
        request: HentFeilutbetalingerFraSimuleringRequest,
    ): ResponseEntity<Ressurs<FeilutbetalingerFraSimulering>> {
        if (featureToggleService.isEnabled(FeatureToggle.SKRU_AV_SIMULERING, true)) {
            logger.info(
                "Tilbakekreving forsøker å hente feilutbetalinger fra tidligere simuleringer, men simulering er skrudd av for familie-oppdrag. Henting av feilutbetaling skal nå gjøres via familie-oppdrag-backend i GCP (http://familie-oppdrag-backend).",
            )
            return serviceUnavailable(
                "Hent feilutbetalinger er skrudd av for familie-oppdrag. All fremtidig henting av feilutbetaling skal gjøres via familie-oppdrag-backend i GCP (http://familie-oppdrag-backend).",
            )
        }
        logger.info(
            "Henter feilutbetalinger for ytelsestype=${request.ytelsestype}, " +
                "fagsak=${request.eksternFagsakId}," +
                " behandlingId=${request.eksternFagsakId}",
        )
        return ok(simuleringTjeneste.hentFeilutbetalinger(request))
    }
}
