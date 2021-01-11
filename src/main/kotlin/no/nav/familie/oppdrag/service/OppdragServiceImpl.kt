package no.nav.familie.oppdrag.service

import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.domene.id
import no.nav.familie.oppdrag.iverksetting.OppdragSender
import no.nav.familie.oppdrag.repository.OppdragLager
import no.nav.familie.oppdrag.repository.OppdragRepository
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("!e2e")
class OppdragServiceImpl(private val oppdragSender: OppdragSender,
                         private val oppdragLagerRepository: OppdragRepository) : OppdragService {

    @Transactional(rollbackFor = [Throwable::class])
    override fun opprettOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag, oppdrag: Oppdrag, versjon: Int) {
        LOG.debug("Legger oppdrag på kø " + oppdrag.id)
        oppdragSender.sendOppdrag(oppdrag)

        LOG.debug("Lagrer oppdrag i databasen " + oppdrag.id)
        oppdragLagerRepository.insert(OppdragLager.lagFraOppdrag(utbetalingsoppdrag, oppdrag, versjon))
    }

    override fun hentStatusForOppdrag(oppdragId: OppdragId): OppdragStatus {
        return oppdragLagerRepository.hentOppdrag(oppdragId.behandlingsId,
                                                  oppdragId.personIdent,
                                                  oppdragId.fagsystem).status
    }

    companion object {

        val LOG = LoggerFactory.getLogger(OppdragServiceImpl::class.java)
    }

}