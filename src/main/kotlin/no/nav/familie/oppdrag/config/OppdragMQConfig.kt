package no.nav.familie.oppdrag.config

import com.ibm.mq.constants.CMQC.MQENC_NATIVE
import com.ibm.mq.jms.MQQueueConnectionFactory
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate
import javax.jms.ConnectionFactory
import javax.jms.JMSException

private const val UTF_8_WITH_PUA = 1208

@Configuration
@Profile("!dev")
class OppdragMQConfig(@Value("\${oppdrag.mq.hostname}") val hostname: String,
                      @Value("\${oppdrag.mq.queuemanager}") val queuemanager: String,
                      @Value("\${oppdrag.mq.channel}") val channel: String,
                      @Value("\${oppdrag.mq.queuename}") val queuename: String,
                      @Value("\${oppdrag.mq.port}") val port: Int,
                      @Value("\${oppdrag.mq.user}") val user: String,
                      @Value("\${oppdrag.mq.password}") val password: String) {

    @Bean
    @Throws(JMSException::class)
    fun mqQueueConnectionFactory(): ConnectionFactory {
        val connectionFactory = MQQueueConnectionFactory()
        connectionFactory.hostName = hostname
        connectionFactory.queueManager = queuemanager
        connectionFactory.channel = channel
        connectionFactory.port = port
        connectionFactory.transportType = WMQ_CM_CLIENT
        connectionFactory.ccsid = UTF_8_WITH_PUA
        connectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE)
        connectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA)

        val cf = UserCredentialsConnectionFactoryAdapter()
        cf.setUsername(user)
        cf.setPassword(password)
        cf.setTargetConnectionFactory(connectionFactory)
        return cf
    }

    @Bean
    fun jmsTemplate(connectionFactory: ConnectionFactory): JmsTemplate {
        val jmsTemplate = JmsTemplate(connectionFactory)
        jmsTemplate.defaultDestinationName = queuename
        return jmsTemplate
    }
}