package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.GrensesnittavstemmingRequest
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingRequest
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingRequestV2
import no.nav.familie.kontrakter.felles.oppdrag.OppdragIdForFagsystem
import no.nav.familie.oppdrag.common.RessursUtils.illegalState
import no.nav.familie.oppdrag.common.RessursUtils.ok
import no.nav.familie.oppdrag.service.GrensesnittavstemmingService
import no.nav.familie.oppdrag.service.KonsistensavstemmingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class AvstemmingController(@Autowired val grensesnittavstemmingService: GrensesnittavstemmingService,
                           @Autowired val konsistensavstemmingService: KonsistensavstemmingService) {

    @Deprecated("Bruk post med body")
    @PostMapping(path = ["/grensesnittavstemming/{fagsystem}"])
    fun sendGrensesnittavstemming(@PathVariable("fagsystem") fagsystem: String,
                                  @RequestParam("fom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fom: LocalDateTime,
                                  @RequestParam("tom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) tom: LocalDateTime
    ): ResponseEntity<Ressurs<String>> {
        LOG.info("Grensesnittavstemming: Kjører for $fagsystem-oppdrag for $fom til $tom")

        return Result.runCatching { grensesnittavstemmingService.utførGrensesnittavstemming(fagsystem, fom, tom) }
                .fold(
                        onFailure = {
                            illegalState("Grensesnittavstemming feilet", it)
                        },
                        onSuccess = {
                            ok("Grensesnittavstemming sendt ok")
                        }

                )
    }

    @PostMapping(path = ["/grensesnittavstemming"])
    fun grensesnittavstemming(@RequestBody request: GrensesnittavstemmingRequest): ResponseEntity<Ressurs<String>> {
        LOG.info("Grensesnittavstemming: Kjører for ${request.fagsystem}-oppdrag fra ${request.fra} til ${request.til}")

        return Result.runCatching { grensesnittavstemmingService.utførGrensesnittavstemming(request) }
                .fold(onFailure = { illegalState("Grensesnittavstemming feilet", it) },
                      onSuccess = { ok("Grensesnittavstemming sendt ok") })
    }

    @Deprecated("Bruk v2")
    @PostMapping(path = ["/konsistensavstemming/{fagsystem}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendKonsistensavstemming(@PathVariable("fagsystem") fagsystem: String,
                                 @RequestBody oppdragIdListe: List<OppdragIdForFagsystem>,
                                 @RequestParam("avstemmingsdato") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                 avstemmingsdato: LocalDateTime
    ): ResponseEntity<Ressurs<String>> {
        return konsistensavstemming(KonsistensavstemmingRequest(fagsystem, oppdragIdListe, avstemmingsdato))
    }

    @Deprecated("Bruk v2")
    @PostMapping(path = ["/konsistensavstemming"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun konsistensavstemming(@RequestBody request: KonsistensavstemmingRequest): ResponseEntity<Ressurs<String>> {
        LOG.info("Konsistensavstemming: Kjører for ${request.fagsystem}-oppdrag for ${request.avstemmingstidspunkt} " +
                 "med ${request.oppdragIdListe.size} antall utbetalingsoppdrag")

        return Result.runCatching {
            konsistensavstemmingService.utførKonsistensavstemming(request)
        }.fold(onFailure = { illegalState("Konsistensavstemming feilet", it) },
               onSuccess = { ok("Konsistensavstemming sendt ok") })
    }

    @PostMapping(path = ["/v2/konsistensavstemming"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun konsistensavstemming(@RequestBody request: KonsistensavstemmingRequestV2): ResponseEntity<Ressurs<String>> {
        LOG.info("Konsistensavstemming: Kjører for ${request.fagsystem}-oppdrag for ${request.avstemmingstidspunkt} " +
                 "med ${request.perioderForBehandlinger.sumOf { it.perioder.size }} antall periodeIder")

        return Result.runCatching {
            konsistensavstemmingService.utførKonsistensavstemming(request)
        }.fold(onFailure = { illegalState("Konsistensavstemming feilet", it) },
               onSuccess = { ok("Konsistensavstemming sendt ok") })
    }

    companion object {

        val LOG: Logger = LoggerFactory.getLogger(AvstemmingController::class.java)
    }

}