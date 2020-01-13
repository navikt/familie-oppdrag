package no.nav.familie.oppdrag.avstemming

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.JmsException
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import java.lang.UnsupportedOperationException

@Service
class AvstemmingSenderMQ(val jmsTemplateAvstemming: JmsTemplate,
                         @Value("\${oppdrag.mq.enabled}") val erEnabled: String) {

    fun sendGrensesnittAvstemming() {
        if (!erEnabled.toBoolean()) {
            LOG.info("MQ-integrasjon mot oppdrag er skrudd av")
            throw UnsupportedOperationException("Kan ikke sende avstemming til oppdrag. Integrasjonen er skrudd av.")
        }

        try {
            jmsTemplateAvstemming.send { session ->
                // TODO: XML for avstemming skal inn her
                val msg = session.createTextMessage("")
                msg
            }
            LOG.info("Sendt Avstemming-XML på kø ${jmsTemplateAvstemming.defaultDestinationName} til OS")
        } catch (e: JmsException) {
            LOG.error("Klarte ikke sende Avstemming til OS. Feil: ", e)
            throw e
        }
    }

    companion object {
        val LOG = LoggerFactory.getLogger(AvstemmingSenderMQ::class.java)
    }
}