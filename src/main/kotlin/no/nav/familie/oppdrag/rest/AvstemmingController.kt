package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.GrensesnittavstemmingRequest
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingRequestV2
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingUtbetalingsoppdrag
import no.nav.familie.oppdrag.common.RessursUtils.illegalState
import no.nav.familie.oppdrag.common.RessursUtils.ok
import no.nav.familie.oppdrag.service.GrensesnittavstemmingService
import no.nav.familie.oppdrag.service.KonsistensavstemmingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
@Validated
class AvstemmingController(@Autowired val grensesnittavstemmingService: GrensesnittavstemmingService,
                           @Autowired val konsistensavstemmingService: KonsistensavstemmingService) {

    @PostMapping(path = ["/grensesnittavstemming"])
    fun grensesnittavstemming(@RequestBody request: GrensesnittavstemmingRequest): ResponseEntity<Ressurs<String>> {
        LOG.info("Grensesnittavstemming: Kjører for ${request.fagsystem}-oppdrag fra ${request.fra} til ${request.til}")

        return Result.runCatching { grensesnittavstemmingService.utførGrensesnittavstemming(request) }
                .fold(onFailure = { illegalState("Grensesnittavstemming feilet", it) },
                      onSuccess = { ok("Grensesnittavstemming sendt ok") })
    }

    @PostMapping(path = ["/v2/konsistensavstemming"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun konsistensavstemming(@RequestBody request: KonsistensavstemmingRequestV2,
                             @RequestParam(name = "sendStartmelding") sendStartmelding: Boolean = true,
                             @RequestParam(name = "sendAvsluttmelding") sendAvsluttmelding: Boolean = true
    ): ResponseEntity<Ressurs<String>> {
        LOG.info("Konsistensavstemming: Kjører for ${request.fagsystem}-oppdrag for ${request.avstemmingstidspunkt} " +
                 "med ${request.perioderForBehandlinger.sumOf { it.perioder.size }} antall periodeIder")

        return Result.runCatching {
            konsistensavstemmingService.utførKonsistensavstemming(request, sendStartmelding, sendAvsluttmelding)
        }.fold(onFailure = { illegalState("Konsistensavstemming feilet", it) },
               onSuccess = { ok("Konsistensavstemming sendt ok") })
    }

    @PostMapping(path = ["/konsistensavstemming"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun konsistensavstemming(@RequestBody request: KonsistensavstemmingUtbetalingsoppdrag): ResponseEntity<Ressurs<String>> {
        LOG.info("Konsistensavstemming: Kjører for ${request.fagsystem}-oppdrag for ${request.avstemmingstidspunkt} " +
                         "med ${request.utbetalingsoppdrag.size} antall oppdrag")

        return Result.runCatching {
            konsistensavstemmingService.utførKonsistensavstemming(request)
        }.fold(onFailure = { illegalState("Konsistensavstemming feilet", it) },
               onSuccess = { ok("Konsistensavstemming sendt ok") })
    }

    companion object {

        val LOG: Logger = LoggerFactory.getLogger(AvstemmingController::class.java)
    }
}