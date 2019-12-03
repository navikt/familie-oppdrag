package no.nav.familie.oppdrag.rest

import no.nav.familie.oppdrag.iverksetting.OppdragSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class KøController(@Autowired val oppdragSender: OppdragSender) {

    @GetMapping("/mq")
    fun sendOppdrag() {
        oppdragSender.sendOppdrag("Test")
    }
}