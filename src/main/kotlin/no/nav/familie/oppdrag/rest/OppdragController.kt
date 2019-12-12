package no.nav.familie.oppdrag.rest

import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class OppdragController(@Autowired val oppdragSender: OppdragSender, @Autowired val oppdragMapper: OppdragMapper) {

    @Deprecated("Dette endepunktet brukes kun for å teste integrasjonen mot OS over MQ")
    @GetMapping("/oppdrag")
    fun sendOppdrag(): String {

        val oppdrag110 = oppdragMapper.tilOppdrag110()
        oppdragSender.sendOppdrag(oppdragMapper.tilOppdrag(oppdrag110))
        return "Oppdrag sendt ok"
    }
}