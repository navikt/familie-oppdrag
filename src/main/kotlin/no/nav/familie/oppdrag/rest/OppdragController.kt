package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.behandlingsIdForFørsteUtbetalingsperiode
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid


@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class OppdragController(@Autowired val oppdragSender: OppdragSender,
                        @Autowired val oppdragMapper: OppdragMapper,
                        @Autowired val oppdragProtokollRepository: OppdragProtokollRepository) {

   @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdrag"])
   fun sendOppdrag(@Valid @RequestBody utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<String>> {
        val oppdrag110 = oppdragMapper.tilOppdrag110(utbetalingsoppdrag)
        val oppdrag = oppdragMapper.tilOppdrag(oppdrag110)

       if (oppdragProtokollRepository.hentEksisterendeOppdrag(utbetalingsoppdrag.fagSystem,
                       utbetalingsoppdrag.behandlingsIdForFørsteUtbetalingsperiode(),
                       utbetalingsoppdrag.aktoer).isNotEmpty()) {
           return ResponseEntity.badRequest().body(Ressurs.failure("Oppdraget finnes fra før"))
       }

        // TODO flytt disse to til en @Transactional + @Service type klasse
        oppdragSender.sendOppdrag(oppdrag)
        oppdragProtokollRepository.save(OppdragProtokoll.lagFraOppdrag(utbetalingsoppdrag, oppdrag))
        return ResponseEntity.ok().body(Ressurs.Companion.success("Oppdrag sendt ok"))
    }
}