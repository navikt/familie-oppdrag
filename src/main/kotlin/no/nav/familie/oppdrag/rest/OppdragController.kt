package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.domene.OppdragId
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.repository.OppdragProtokollStatus
import no.nav.familie.oppdrag.service.OppdragService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid


@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class OppdragController(@Autowired val oppdragService: OppdragService,
                        @Autowired val oppdragMapper: OppdragMapper) {

   @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdrag"])
   fun sendOppdrag(@Valid @RequestBody utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<String>> {
        val oppdrag110 = oppdragMapper.tilOppdrag110(utbetalingsoppdrag)
        val oppdrag = oppdragMapper.tilOppdrag(oppdrag110)

        oppdragService.opprettOppdrag(utbetalingsoppdrag,oppdrag)
        return ResponseEntity.ok().body(Ressurs.Companion.success("Oppdrag sendt ok"))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/status"])
    fun hentStatus(@Valid @RequestBody oppdragId: OppdragId): ResponseEntity<Ressurs<OppdragProtokollStatus>> {
        return Result.runCatching { oppdragService.hentStatusForOppdrag(oppdragId) }
                .fold(
                        onFailure = {
                            ResponseEntity
                                    .status(HttpStatus.NOT_FOUND)
                                    .body(Ressurs.failure(errorMessage = "Fant ikke oppdrag med id $oppdragId"))
                        },
                        onSuccess = {
                            ResponseEntity.ok(Ressurs.success(it))
                        }
                )
    }

    @PostMapping(path = ["/grensesnittavstemming/{fagsystem}"])
    fun sendGrensesnittavstemming(@PathVariable("fagsystem") fagsystem: String,
                       @RequestParam("fom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fom: LocalDateTime,
                       @RequestParam("tom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) tom: LocalDateTime
    ): ResponseEntity<Ressurs<String>> {
        LOG.info("Grensesnittavstemming: Kjører for $fagsystem-oppdrag for $fom til $tom")
        // TODO Grensesnittavstemming skal inn her
        return ResponseEntity.ok().body(Ressurs.success("Grensesnittavstemming sendt ok"))
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragController::class.java)
    }
}