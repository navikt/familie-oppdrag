package no.nav.familie.oppdrag.iverksetting

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.jms.core.JmsTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.JmsException


@Service
class OppdragSender(@Autowired val jmsTemplate: JmsTemplate,
                    @Value("\${ibm.mq.enabled}") val erEnabled: String,
                    @Value("\${ibm.mq.queuename}") val køNavn: String) {

    fun sendOppdrag(oppdrag110Xml: String): String {
        if (!erEnabled.toBoolean()) {
            LOG.info("MQ-integrasjon mot oppdrag er skrudd av")
            return oppdrag110Xml
        }

        try {
            LOG.info("Default destination er: {}", jmsTemplate.defaultDestinationName)
            jmsTemplate.convertAndSend(køNavn, oppdrag110Xml)
            LOG.info("Sender Oppdrag110-XML over MQ til OS")
        } catch (e: JmsException) {
            LOG.error("Klarte ikke sende Oppdrag til OS. Feil: ", e)
            throw e
        }
        return oppdrag110Xml
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragSender::class.java)
    }
}