package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.oppdrag.service.AvstemmingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class AvstemmingController(@Autowired val avstemmingService: AvstemmingService) {

    @PostMapping(path = ["/grensesnittavstemming/{fagsystem}"])
    fun sendGrensesnittavstemming(@PathVariable("fagsystem") fagsystem: String,
                                  @RequestParam("fom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fom: LocalDateTime,
                                  @RequestParam("tom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) tom: LocalDateTime
    ): ResponseEntity<Ressurs<String>> {
        LOG.info("Grensesnittavstemming: Kjører for $fagsystem-oppdrag for $fom til $tom")

        return Result.runCatching {  avstemmingService.utførGrensesnittavstemming(fagsystem, fom, tom) }
                .fold(
                        onFailure = {
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Ressurs.failure("Grensesnittavstemming feilet", it))
                        },
                        onSuccess = {
                            ResponseEntity.ok(Ressurs.Companion.success("Grensesnittavstemming sendt ok"))
                        }

                )
    }

    companion object {
        val LOG = LoggerFactory.getLogger(AvstemmingController::class.java)
    }

}