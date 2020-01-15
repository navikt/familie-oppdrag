package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.behandlingsIdForFørsteUtbetalingsperiode
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
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
class OppdragController(@Autowired val oppdragService: OppdragService,
                        @Autowired val oppdragMapper: OppdragMapper) {

   @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdrag"])
   fun sendOppdrag(@Valid @RequestBody utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<String>> {
        val oppdrag110 = oppdragMapper.tilOppdrag110(utbetalingsoppdrag)
        val oppdrag = oppdragMapper.tilOppdrag(oppdrag110)

        oppdragService.opprettOppdrag(utbetalingsoppdrag,oppdrag)
        return ResponseEntity.ok().body(Ressurs.Companion.success("Oppdrag sendt ok"))
    }
}