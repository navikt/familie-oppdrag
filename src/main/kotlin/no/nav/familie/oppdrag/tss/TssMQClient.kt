package no.nav.familie.oppdrag.tss

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jms.JmsException
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import org.springframework.stereotype.Service
import java.util.UUID
import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session

@Service
class TssMQClient(@Qualifier("jmsTemplateTss") private val jmsTemplateTss: JmsTemplate) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun kallTss(rawRequest: String): String {
        logger.info("gjør kall mot tss ${jmsTemplateTss.defaultDestinationName} ${jmsTemplateTss.connectionFactory}")

        try {
            val response: Message? = jmsTemplateTss.sendAndReceive(
                MessageCreator { session: Session ->
                    val requestMessage = session.createTextMessage(rawRequest)
                    requestMessage.jmsCorrelationID = UUID.randomUUID().toString()
                    requestMessage
                }
            )

            return if (response == null) {
                logger.error("En feil oppsto i kallet til TSS. Response var null (timeout?)")
                error("En feil oppsto i kallet til TSS. Response var null (timeout?)")
            } else {
                response.getBody(String::class.java)
            }
        } catch (exception: Exception) {
            logger.info("Feil ved sending", exception)
            when (exception) {
                is JmsException, is JMSException -> {
                    throw java.lang.RuntimeException("En feil oppsto i kallet til TSS. Response var null.", exception)
                }
                else -> throw exception
            }
        }
    }
}
