package no.nav.familie.oppdrag.iverksetting

import no.nav.familie.oppdrag.domene.OppdragId
import no.nav.familie.oppdrag.domene.id
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.familie.oppdrag.repository.OppdragProtokollStatus
import no.nav.familie.oppdrag.repository.protokollStatus
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import java.util.*
import javax.jms.TextMessage

@Service
class OppdragMottaker(
        @Autowired val oppdragProtokollRepository: OppdragProtokollRepository,
        val env: Environment) {

    // jmsListenerContainerFactory sørger for en transaksjon. Exception her betyr at meldingen blir liggende på køen
    @JmsListener(destination = "\${oppdrag.mq.mottak}", containerFactory = "jmsListenerContainerFactory")
    fun mottaKvitteringFraOppdrag(melding: TextMessage) {
        var svarFraOppdrag = melding.text as String
        if (!env.activeProfiles.contains("dev")) {
            svarFraOppdrag = svarFraOppdrag.replace("oppdrag xmlns", "ns2:oppdrag xmlns:ns2")
        }

        val kvittering = lesKvittering(svarFraOppdrag)
        val oppdragId = kvittering.id()
        val status = kvittering.protokollStatus()
        val sendtOppdrag: Optional<OppdragProtokoll?> = oppdragProtokollRepository.findById(oppdragId)

        when {
            sendtOppdrag.isEmpty -> {
                // TODO: Fant ikke oppdrag. Det er VELDIG feil
            }
            sendtOppdrag.get().status != OppdragProtokollStatus.LAGT_PÅ_KØ -> {
                // TODO: Oppdraget har en status vi ikke venter. Det er GANSKE så feil
            }
            else -> {
                val oppdatertOppdrag = sendtOppdrag.get().copy(status = status)
                oppdragProtokollRepository.save(oppdatertOppdrag)
            }
        }
    }

    fun lesKvittering(svarFraOppdrag: String): Oppdrag {
        val oppdragKvittering = Jaxb().tilOppdrag(svarFraOppdrag)
        val status = oppdragKvittering.status()
        val fagsakId = oppdragKvittering.id().fagsystem
        val svarMelding = hentMelding(oppdragKvittering)
        LOG.info("Mottatt melding på kvitteringskø for fagsak $fagsakId: Status $status, svar $svarMelding")
        return oppdragKvittering
    }

    private fun hentMelding(kvittering: Oppdrag): String {
        return kvittering.mmel?.beskrMelding ?: "Beskrivende melding ikke satt fra OS"
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragMottaker::class.java)
    }
}