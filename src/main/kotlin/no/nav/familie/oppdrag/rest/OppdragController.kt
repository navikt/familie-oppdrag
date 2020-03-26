package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.domene.OppdragId
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.repository.OppdragStatus
import no.nav.familie.oppdrag.service.OppdragService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class OppdragController(@Autowired val oppdragService: OppdragService,
                        @Autowired val oppdragMapper: OppdragMapper) {

   @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdrag"])
   fun sendOppdrag(@Valid @RequestBody utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<String>> {
       return Result.runCatching {
           val oppdrag110 = oppdragMapper.tilOppdrag110(utbetalingsoppdrag)
           val oppdrag = oppdragMapper.tilOppdrag(oppdrag110)

           oppdragService.opprettOppdrag(utbetalingsoppdrag,oppdrag, 0)
       }.fold(
               onFailure = {
                   SECURE_LOG.error("Feil ved iverksetting av oppdrag:", it)
                   ResponseEntity
                           .status(HttpStatus.INTERNAL_SERVER_ERROR)
                           .body(Ressurs.failure(errorMessage = "Klarte ikke sende oppdrag for saksnr ${utbetalingsoppdrag.saksnummer}"))
               },
               onSuccess = {
                   ResponseEntity.ok(Ressurs.success("Oppdrag sendt OK"))
               }
       )
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdragPaaNytt/{versjon}"])
    fun sendOppdragPåNytt(@Valid @RequestBody utbetalingsoppdrag: Utbetalingsoppdrag,
                          @PathVariable versjon: Int): ResponseEntity<Ressurs<String>> {
        return Result.runCatching {
            val oppdrag110 = oppdragMapper.tilOppdrag110(utbetalingsoppdrag)
            val oppdrag = oppdragMapper.tilOppdrag(oppdrag110)

            oppdragService.opprettOppdrag(utbetalingsoppdrag,oppdrag, versjon)
        }.fold(
                onFailure = {
                    SECURE_LOG.error("Feil ved iverksetting av oppdrag:", it)
                    ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Ressurs.failure(errorMessage = "Klarte ikke sende oppdrag for saksnr ${utbetalingsoppdrag.saksnummer}"))
                },
                onSuccess = {
                    ResponseEntity.ok(Ressurs.success("Oppdrag sendt OK"))
                }
        )
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/status"])
    fun hentStatus(@Valid @RequestBody oppdragId: OppdragId): ResponseEntity<Ressurs<OppdragStatus>> {
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

    companion object {
        val SECURE_LOG = LoggerFactory.getLogger("secureLogger")
    }
}