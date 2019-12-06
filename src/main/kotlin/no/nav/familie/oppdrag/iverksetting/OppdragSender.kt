package no.nav.familie.oppdrag.iverksetting

import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.jms.core.JmsTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.JmsException
import java.lang.UnsupportedOperationException


@Service
class OppdragSender(@Autowired val jmsTemplate: JmsTemplate,
                    @Value("\${oppdrag.mq.enabled}") val erEnabled: String,
                    @Value("\${oppdrag.mq.queuename}") val køNavn: String) {

    fun sendOppdrag(oppdrag: Oppdrag): String {
        if (!erEnabled.toBoolean()) {
            LOG.info("MQ-integrasjon mot oppdrag er skrudd av")
            throw UnsupportedOperationException("Kan ikke sende melding til oppdrag. Integrasjonen er skrudd av.")
        }

        val oppdragXml = Jaxb().tilXml(oppdrag)
        try {
            jmsTemplate.convertAndSend(køNavn, oppdragXml)
            LOG.info("Sender Oppdrag110-XML over MQ til OS")
        } catch (e: JmsException) {
            LOG.error("Klarte ikke sende Oppdrag til OS. Feil: ", e)
            throw e
        }
        return oppdragXml
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragSender::class.java)
    }
}