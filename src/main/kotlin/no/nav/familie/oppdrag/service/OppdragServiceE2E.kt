package no.nav.familie.oppdrag.service

import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.domene.id
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.repository.OppdragLager
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.rest.RestSendOppdrag
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("e2e")
class OppdragServiceE2E(
        @Autowired private val oppdragLagerRepository: OppdragLagerRepository) : OppdragService {

    @Transactional(rollbackFor = [Throwable::class])
    override fun opprettOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag, versjon: Int) {
        val oppdrag = utbetalingsoppdrag.somOppdragSkjema

        LOG.debug("Lagrer oppdrag i databasen " + oppdrag.id)
        oppdragLagerRepository.opprettOppdrag(OppdragLager.lagFraOppdrag(utbetalingsoppdrag, oppdrag), versjon)

        LOG.debug("Kvittering mottat ok " + oppdrag.id)
        oppdragLagerRepository.oppdaterStatus(oppdrag.id, OppdragStatus.KVITTERT_OK)
    }

    @Transactional(rollbackFor = [Throwable::class])
    override fun opprettOppdragV2(restSendOppdrag: RestSendOppdrag, versjon: Int) {

        val oppdrag = restSendOppdrag.utbetalingsoppdrag.somOppdragSkjema

        LOG.debug("Lagrer oppdrag i databasen " + oppdrag.id)
        oppdragLagerRepository.opprettOppdrag(OppdragLager.lagFraOppdragV2(utbetalingsoppdrag = restSendOppdrag.utbetalingsoppdrag,
                                                                           gjeldendeBehandlingId = restSendOppdrag.gjeldendeBehandlingId.toString(),
                                                                           oppdrag = oppdrag), versjon)

        LOG.debug("Kvittering mottat ok " + oppdrag.id)
        oppdragLagerRepository.oppdaterStatus(oppdrag.id, OppdragStatus.KVITTERT_OK)
    }

    override fun hentStatusForOppdrag(oppdragId: OppdragId): OppdragStatus {
        return oppdragLagerRepository.hentOppdrag(oppdragId).status
    }

    companion object {

        val LOG = LoggerFactory.getLogger(OppdragServiceE2E::class.java)

        val Utbetalingsoppdrag.somOppdragSkjema: Oppdrag
            get() {
                val oppdrag110 = OppdragMapper.tilOppdrag110(this)
                return OppdragMapper.tilOppdrag(oppdrag110)
            }

    }
}