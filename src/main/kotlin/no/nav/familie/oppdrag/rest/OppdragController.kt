package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.common.RessursUtils.illegalState
import no.nav.familie.oppdrag.common.RessursUtils.notFound
import no.nav.familie.oppdrag.common.RessursUtils.ok
import no.nav.familie.oppdrag.service.OppdragService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class OppdragController(@Autowired val oppdragService: OppdragService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdrag"])
    fun sendOppdrag(@Valid @RequestBody utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<String>> {
        return Result.runCatching {

            oppdragService.opprettOppdrag(utbetalingsoppdrag, 0)
        }.fold(
                onFailure = {
                    illegalState("Klarte ikke sende oppdrag for saksnr ${utbetalingsoppdrag.saksnummer}", it)
                },
                onSuccess = {
                    ok("Oppdrag sendt OK")
                }
        )
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdrag/v2"])
    fun sendOppdragV2(@Valid @RequestBody restSendOppdrag: RestSendOppdrag): ResponseEntity<Ressurs<String>> {
        return Result.runCatching {
            oppdragService.opprettOppdragV2(restSendOppdrag, 0)
        }.fold(
                onFailure = {
                    illegalState("Klarte ikke sende oppdrag for saksnr ${restSendOppdrag.utbetalingsoppdrag.saksnummer}", it)
                },
                onSuccess = {
                    ok("Oppdrag sendt OK")
                }
        )
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdragPaaNytt/{versjon}"])
    fun sendOppdragPåNytt(@Valid @RequestBody restSendOppdrag: RestSendOppdrag,
                          @PathVariable versjon: Int): ResponseEntity<Ressurs<String>> {
        return Result.runCatching {
            oppdragService.opprettOppdragV2(restSendOppdrag, versjon)
        }.fold(
                onFailure = {
                    illegalState("Klarte ikke sende oppdrag for saksnr ${restSendOppdrag.utbetalingsoppdrag.saksnummer}", it)
                },
                onSuccess = {
                    ok("Oppdrag sendt OK")
                }
        )
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/status"])
    fun hentStatus(@Valid @RequestBody oppdragId: OppdragId): ResponseEntity<Ressurs<OppdragStatus>> {
        return Result.runCatching { oppdragService.hentStatusForOppdrag(oppdragId) }
                .fold(
                        onFailure = {
                            notFound("Fant ikke oppdrag med id $oppdragId")
                        },
                        onSuccess = {
                            ok(it)
                        }
                )
    }
}

data class RestSendOppdrag(
        val utbetalingsoppdrag: Utbetalingsoppdrag,
        val gjeldendeBehandlingId: Long
)