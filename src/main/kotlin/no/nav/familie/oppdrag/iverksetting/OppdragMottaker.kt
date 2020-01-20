package no.nav.familie.oppdrag.iverksetting

import no.nav.familie.oppdrag.domene.id
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.familie.oppdrag.repository.OppdragProtokollStatus
import no.nav.familie.oppdrag.repository.protokollStatus
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.jms.TextMessage

@Service
class OppdragMottaker(
        val oppdragProtokollRepository: OppdragProtokollRepository,
        val env: Environment
){
    internal var LOG = LoggerFactory.getLogger(OppdragMottaker::class.java)

    @Transactional
    @JmsListener(destination = "\${oppdrag.mq.mottak}", containerFactory = "jmsListenerContainerFactory")
    fun mottaKvitteringFraOppdrag(melding: TextMessage) {
        var svarFraOppdrag = melding.text as String
        if (!env.activeProfiles.contains("dev")) {
            svarFraOppdrag = svarFraOppdrag.replace("oppdrag xmlns", "ns2:oppdrag xmlns:ns2")
        }

        val kvittering = lesKvittering(svarFraOppdrag)
        val oppdragId = kvittering.id
        LOG.info("Mottatt melding på kvitteringskø for fagsak ${oppdragId}: Status ${kvittering.status}, " +
                 "svar ${kvittering.mmel?.beskrMelding ?: "Beskrivende melding ikke satt fra OS"}")

        LOG.info("Henter oppdrag ${oppdragId} fra databasen")
        val sendteOppdrag: OppdragProtokoll = oppdragProtokollRepository.hentOppdrag(oppdragId)

        if (sendteOppdrag.status != OppdragProtokollStatus.LAGT_PÅ_KØ) {
            // TODO: Oppdraget har en status vi ikke venter. Det er GANSKE så feil
            LOG.warn("Oppdraget tilknyttet mottatt kvittering har uventet status i databasen. Oppdraget er: ${oppdragId}. " +
                    "Status i databasen er ${sendteOppdrag.status}. " +
                    "Lagrer likevel oppdatert oppdrag i databasen med ny status ${kvittering.protokollStatus}")
            oppdragProtokollRepository.oppdaterStatus(oppdragId, kvittering.protokollStatus)
        } else  {
            LOG.debug("Lagrer oppdatert oppdrag ${oppdragId} i databasen med ny status ${kvittering.protokollStatus}")
            oppdragProtokollRepository.oppdaterStatus(oppdragId, kvittering.protokollStatus)
        }
    }

    fun lesKvittering(svarFraOppdrag: String): Oppdrag {
        val kvittering = Jaxb().tilOppdrag(svarFraOppdrag)
        return kvittering
    }
}