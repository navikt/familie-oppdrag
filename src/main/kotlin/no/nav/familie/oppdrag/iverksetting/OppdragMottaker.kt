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
        LOG.info("Lytter på kvitteringskø: {}", jmsTemplateInngående.defaultDestinationName)

        val melding = jmsTemplateInngående.receiveAndConvert() as String
        val gyldigXmlMelding = melding.replace("oppdrag xmlns", "ns2:oppdrag xmlns:ns2")
        LOG.info("Mottatt melding fra OS: {}", gyldigXmlMelding)

        val oppdragKvittering = Jaxb().tilOppdrag(gyldigXmlMelding)
        val status = hentStatus(oppdragKvittering)
        val svarMelding = hentMelding(oppdragKvittering)
        LOG.info("Unmarshallet melding på kvitteringskø: status $status og svar $svarMelding")
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