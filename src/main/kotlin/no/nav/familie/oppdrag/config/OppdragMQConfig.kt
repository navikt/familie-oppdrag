package no.nav.familie.oppdrag.config

import com.ibm.mq.constants.CMQC.MQENC_NATIVE
import com.ibm.mq.jms.MQQueueConnectionFactory
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate
import javax.jms.ConnectionFactory
import javax.jms.JMSException

private const val UTF_8_WITH_PUA = 1208

@Configuration
class OppdragMQConfig(@Value("\${oppdrag.mq.hostname}") val hostname: String,
                      @Value("\${oppdrag.mq.queuemanager}") val queuemanager: String,
                      @Value("\${oppdrag.mq.channel}") val channel: String,
                      @Value("\${oppdrag.mq.send}") val sendQueue: String,
                      @Value("\${oppdrag.mq.port}") val port: Int,
                      @Value("\${oppdrag.mq.user}") val user: String,
                      @Value("\${oppdrag.mq.password}") val password: String) {

    @Bean
    @Throws(JMSException::class)
    fun mqQueueConnectionFactory(): ConnectionFactory {
        val targetFactory = MQQueueConnectionFactory()
        targetFactory.hostName = hostname
        targetFactory.queueManager = queuemanager
        targetFactory.channel = channel
        targetFactory.port = port
        targetFactory.transportType = WMQ_CM_CLIENT
        targetFactory.ccsid = UTF_8_WITH_PUA
        targetFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE)
        targetFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA)

        val cf = UserCredentialsConnectionFactoryAdapter()
        cf.setUsername(user)
        cf.setPassword(password)
        cf.setTargetConnectionFactory(targetFactory)
        return cf
    }

    @Bean
    fun jmsTemplateUtgående(connectionFactory: ConnectionFactory): JmsTemplate {
        return JmsTemplate(connectionFactory).apply { defaultDestinationName = sendQueue }
    }
}