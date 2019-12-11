package no.nav.familie.oppdrag.iverksetting

import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service

@Service
class OppdragMottaker(@Autowired val jmsTemplateInngående: JmsTemplate) {

    @JmsListener(destination = "\${oppdrag.mq.mottak}")
    fun mottaKvitteringFraOppdrag() {
        val melding = jmsTemplateInngående.receiveAndConvert() as String
        val gyldigXmlMelding = melding.replace("oppdrag xmlns", "ns2:oppdrag xmlns:ns2")

        val oppdragKvittering = Jaxb().tilOppdrag(gyldigXmlMelding)
        val status = hentStatus(oppdragKvittering)
        val fagsakId = hentFagsystemId(oppdragKvittering)
        val svarMelding = hentMelding(oppdragKvittering)
        LOG.info("Mottatt melding på kvitteringskø for fagsak $fagsakId: Status $status, svar $svarMelding")
    }

    private fun hentFagsystemId(kvittering: Oppdrag): String {
        return kvittering.oppdrag110.fagsystemId
    }

    private fun hentStatus(kvittering: Oppdrag): Status {
        return Status.fromKode(kvittering.mmel.alvorlighetsgrad)
    }

    private fun hentMelding(kvittering: Oppdrag): String {
        return kvittering.mmel.beskrMelding
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragMottaker::class.java)
    }
}