package no.nav.familie.oppdrag.service

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.domene.OppdragId
import no.nav.familie.oppdrag.domene.id
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import no.nav.familie.oppdrag.repository.OppdragLager
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.repository.OppdragStatus
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OppdragService(
        @Autowired private val oppdragSender: OppdragSender,
        @Autowired private val oppdragLagerRepository: OppdragLagerRepository) {

    @Transactional(rollbackFor = [Throwable::class])
    fun opprettOppdrag(utbetalingsoppdrag : Utbetalingsoppdrag, oppdrag: Oppdrag, versjon: Int) {

        LOG.debug("Legger oppdrag på kø "+oppdrag.id)
        oppdragSender.sendOppdrag(oppdrag)

        LOG.debug("Lagrer oppdrag i databasen "+oppdrag.id)
        oppdragLagerRepository.opprettOppdrag(OppdragLager.lagFraOppdrag(utbetalingsoppdrag, oppdrag), versjon)
     }

    fun hentStatusForOppdrag(oppdragId: OppdragId): OppdragStatus {
        return oppdragLagerRepository.hentOppdrag(oppdragId).status
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragService::class.java)
    }

}