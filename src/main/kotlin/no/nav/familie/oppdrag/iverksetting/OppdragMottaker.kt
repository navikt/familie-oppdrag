package no.nav.familie.oppdrag.iverksetting

import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import javax.jms.TextMessage

@Service
class OppdragMottaker(val env: Environment) {

    @JmsListener(destination = "\${oppdrag.mq.mottak}")
    fun mottaKvitteringFraOppdrag(melding: TextMessage): Status {
        var svarFraOppdrag = melding.text as String
        if (!env.activeProfiles.contains("dev")) {
            svarFraOppdrag = svarFraOppdrag.replace("oppdrag xmlns", "ns2:oppdrag xmlns:ns2")
        }

        val oppdragKvittering = Jaxb().tilOppdrag(svarFraOppdrag)

        val status = hentStatus(oppdragKvittering)
        val fagsakId = hentFagsakId(oppdragKvittering)
        val svarMelding = hentMelding(oppdragKvittering)
        LOG.info("Mottatt melding på kvitteringskø for fagsak $fagsakId: Status $status, svar $svarMelding")
        return status
    }

    private fun hentFagsakId(kvittering: Oppdrag): String {
        return kvittering.oppdrag110.fagsystemId ?: "Ukjent"
    }

    private fun hentStatus(kvittering: Oppdrag): Status {
        return Status.fraKode(kvittering.mmel.alvorlighetsgrad)
    }

    private fun hentMelding(kvittering: Oppdrag): String {
        return kvittering.mmel.beskrMelding ?: "Beskrivende melding ikke satt fra OS"
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragMottaker::class.java)
    }
}