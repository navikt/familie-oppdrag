package no.nav.familie.oppdrag.iverksetting

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.jms.core.JmsTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value


@Service
class OppdragSender(@Autowired val jmsTemplate: JmsTemplate, @Value("\${oppdrag.mq.enabled}") val erEnabled: String) {

    fun sendOppdrag(oppdrag110Xml: String) {
        if (!erEnabled.toBoolean()) {
            LOG.info("MQ-integrasjon mot oppdrag er skrudd av")
            return
        }
        jmsTemplate.send { session -> session.createTextMessage(oppdrag110Xml) }
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragSender::class.java)
    }
}