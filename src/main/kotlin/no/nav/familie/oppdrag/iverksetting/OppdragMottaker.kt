package no.nav.familie.oppdrag.iverksetting

import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service

@Service
class OppdragMottaker(@Autowired val jmsTemplateInngående: JmsTemplate,
                      @Autowired val env: Environment) {

    @JmsListener(destination = "\${oppdrag.mq.mottak}")
    fun mottaKvitteringFraOppdrag() {
        var melding = jmsTemplateInngående.receiveAndConvert() as String
        if (!env.activeProfiles.contains("dev")) {
            melding = melding.replace("oppdrag xmlns", "ns2:oppdrag xmlns:ns2")
            val oppdragKvittering = Jaxb().tilOppdrag(melding)

            val status = hentStatus(oppdragKvittering)
            val fagsakId = hentFagsakId(oppdragKvittering)
            val svarMelding = hentMelding(oppdragKvittering)
            LOG.info("Mottatt melding på kvitteringskø for fagsak $fagsakId: Status $status, svar $svarMelding")
        } else {
            LOG.info("Mottatt melding på kvitteringskø $melding")
        }
    }

    private fun hentFagsakId(kvittering: Oppdrag): String {
        return kvittering.oppdrag110.fagsystemId ?: "Ukjent"
    }

    private fun hentStatus(kvittering: Oppdrag): Status {
        return Status.fraKode(kvittering.mmel.alvorlighetsgrad)
    }

    private fun hentMelding(kvittering: Oppdrag): String {
        return kvittering.mmel.beskrMelding
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragMottaker::class.java)
    }
}