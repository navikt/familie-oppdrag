package no.nav.familie.oppdrag.avstemming

import no.nav.familie.oppdrag.grensesnittavstemming.JaxbAvstemmingsdata
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Avstemmingsdata
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.JmsException
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service

@Service
class AvstemmingSenderMQ(val jmsTemplateAvstemming: JmsTemplate,
                         @Value("\${oppdrag.mq.enabled}") val erEnabled: String) : AvstemmingSender {

    override fun sendGrensesnittAvstemming(avstemmingsdata: Avstemmingsdata) {
        if (!erEnabled.toBoolean()) {
            LOG.info("MQ-integrasjon mot oppdrag er skrudd av")
            throw UnsupportedOperationException("Kan ikke sende avstemming til oppdrag. Integrasjonen er skrudd av.")
        }

        val avstemmingXml = JaxbAvstemmingsdata.tilXml(avstemmingsdata)
        try {
/*            jmsTemplateAvstemming.send { session ->
                session.createProducer(
                        MQQueue(jmsTemplateAvstemming.defaultDestinationName).apply {
                            targetClient = WMQConstants.WMQ_CLIENT_NONJMS_MQ
                        })
                val msg = session.createTextMessage(avstemmingXml)
                msg
            }*/
            jmsTemplateAvstemming.convertAndSend(
                    "queue:///${jmsTemplateAvstemming.defaultDestinationName}?targetClient=1",
                    avstemmingXml
            )
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