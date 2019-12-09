package no.nav.familie.oppdrag.iverksetting

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service

@Service
class OppdragMottaker(@Autowired val jmsTemplateInngående: JmsTemplate) {

    @JmsListener(destination = "\${oppdrag.mq.mottak}")
    fun mottaKvitteringFraOppdrag() {

        val melding = jmsTemplateInngående.receiveAndConvert()
        LOG.info("Mottatt melding på kvitteringskø: {}", melding)
    }

    companion object {
        val LOG = LoggerFactory.getLogger(OppdragMottaker::class.java)
    }
}