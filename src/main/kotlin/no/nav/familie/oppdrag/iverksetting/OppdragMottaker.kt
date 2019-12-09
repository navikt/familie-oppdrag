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

        val melding = jmsTemplateInngående.receiveAndConvert()

        val oppdragKvittering = Jaxb().tilOppdrag(melding as String)
        val status = hentStatus(oppdragKvittering)
        val svarMelding = hentMelding(oppdragKvittering)
        LOG.info("Mottatt melding på kvitteringskø med status $status og svar $svarMelding")
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