package no.nav.familie.oppdrag.service

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.behandlingsIdForFørsteUtbetalingsperiode
import no.nav.familie.oppdrag.domene.id
import no.nav.familie.oppdrag.iverksetting.OppdragMottaker
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException

@Service
class OppdragService(
        @Autowired private val oppdragSender: OppdragSender,
        @Autowired private val oppdragProtokollRepository: OppdragProtokollRepository) {

    @Transactional(rollbackFor = [Throwable::class])
    fun opprettOppdrag(utbetalingsoppdrag : Utbetalingsoppdrag, oppdrag: Oppdrag) {

        LOG.debug("Legger oppdrag på kø "+oppdrag.id)
        oppdragSender.sendOppdrag(oppdrag)

        LOG.debug("Lagrer oppdrag i databasen "+oppdrag.id)
        oppdragProtokollRepository.lagreOppdrag(OppdragProtokoll.lagFraOppdrag(utbetalingsoppdrag, oppdrag))

     }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragService::class.java)
    }

}