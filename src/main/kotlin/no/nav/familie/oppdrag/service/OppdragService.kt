package no.nav.familie.oppdrag.service

import no.nav.familie.oppdrag.iverksetting.OppdragSender
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OppdragService(
        @Autowired private val oppdragSender: OppdragSender,
        @Autowired private val oppdragProtokollRepository: OppdragProtokollRepository) {

    @Transactional
    fun opprettOppdrag(oppdrag : Oppdrag) {
        oppdragSender.sendOppdrag(oppdrag)
        oppdragProtokollRepository.save(OppdragProtokoll.lagFraOppdrag(oppdrag))
    }
}