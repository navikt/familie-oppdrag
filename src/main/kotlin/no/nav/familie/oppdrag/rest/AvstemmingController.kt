package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.oppdrag.common.RessursUtils.illegalState
import no.nav.familie.oppdrag.common.RessursUtils.ok
import no.nav.familie.oppdrag.service.GrensesnittavstemmingService
import no.nav.familie.oppdrag.service.KonsistensavstemmingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
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

    @PostMapping(path = ["/konsistensavstemming/{fagsystem}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendKonsistensavstemming(@PathVariable("fagsystem") fagsystem: String,
                                 @RequestBody oppdragIdListe: List<OppdragIdForFagsystem>,
                                 @RequestParam("avstemmingsdato") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                 avstemmingsdato: LocalDateTime
    ): ResponseEntity<Ressurs<String>> {
        LOG.info("Konsistensavstemming: Kjører for $fagsystem-oppdrag for $avstemmingsdato med ${oppdragIdListe.size} antall utbetalingsoppdrag")

        return Result.runCatching {
            konsistensavstemmingService.utførKonsistensavstemming(fagsystem,
                                                                  oppdragIdListe,
                                                                  avstemmingsdato)
        }
                .fold(
                        onFailure = {
                            illegalState("Konsistensavstemming feilet", it)
                        },
                        onSuccess = {
                            ok("Konsistensavstemming sendt ok")
                        }
                )
    }

    companion object {

        val LOG = LoggerFactory.getLogger(AvstemmingController::class.java)
    }

}