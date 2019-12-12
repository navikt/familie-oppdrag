package no.nav.familie.oppdrag.rest

import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api")
class OppdragController(@Autowired val oppdragSender: OppdragSender,
                        @Autowired val oppdragMapper: OppdragMapper,
                        @Autowired val oppdragProtokollRepository: OppdragProtokollRepository) {

    @Deprecated("Dette endepunktet brukes kun for å teste integrasjonen mot OS over MQ")
    @GetMapping("/oppdrag")
    fun sendOppdrag(): String {

        val oppdrag110 = oppdragMapper.tilOppdrag110()
        val oppdrag = oppdragMapper.tilOppdrag(oppdrag110)

        // TODO flytt disse to til en @Transactional + @Service type klasse
        oppdragSender.sendOppdrag(oppdrag)
        oppdragProtokollRepository.save(OppdragProtokoll.lagFraOppdrag(oppdrag))
        return "Oppdrag sendt ok"
    }
}