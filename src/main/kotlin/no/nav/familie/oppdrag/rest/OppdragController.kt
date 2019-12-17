package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class OppdragController(@Autowired val oppdragSender: OppdragSender, @Autowired val oppdragMapper: OppdragMapper) {

   @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/oppdrag"])
   fun sendOppdrag(@Valid @RequestBody utbetalingsoppdrag: Utbetalingsoppdrag): ResponseEntity<Ressurs<String>> {
        val oppdrag110 = oppdragMapper.tilOppdrag110(utbetalingsoppdrag)
        oppdragSender.sendOppdrag(oppdragMapper.tilOppdrag(oppdrag110))
        return ResponseEntity.ok().body(Ressurs.Companion.success("Oppdrag sendt ok"))
    }
}