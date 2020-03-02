package no.nav.familie.oppdrag.iverksetting

import no.nav.familie.oppdrag.domene.id
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.repository.OppdragStatus
import no.nav.familie.oppdrag.repository.oppdragStatus
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.jms.TextMessage

@Service
class OppdragMottaker(
        val oppdragLagerRepository: OppdragLagerRepository,
        val env: Environment
) {
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

        LOG.debug("Henter oppdrag ${oppdragId} fra databasen")

        val førsteOppdragUtenKvittering = oppdragLagerRepository.hentAlleVersjonerAvOppdrag(oppdragId)
                .find { oppdrag -> oppdrag.status == OppdragStatus.LAGT_PÅ_KØ }
        if (førsteOppdragUtenKvittering == null) {
            LOG.warn("Oppdraget tilknyttet mottatt kvittering har uventet status i databasen. Oppdraget er: $oppdragId")
            return
        }

        if (kvittering.mmel != null) {
            oppdragLagerRepository.oppdaterKvitteringsmelding(oppdragId, kvittering.mmel, førsteOppdragUtenKvittering.versjon)
        }

        LOG.debug("Lagrer oppdatert oppdrag ${oppdragId} i databasen med ny status ${kvittering.oppdragStatus}")
        oppdragLagerRepository.oppdaterStatus(oppdragId, kvittering.oppdragStatus, førsteOppdragUtenKvittering.versjon)
    }

    fun lesKvittering(svarFraOppdrag: String): Oppdrag {
        val kvittering = Jaxb.tilOppdrag(svarFraOppdrag)
        return kvittering
    }
}