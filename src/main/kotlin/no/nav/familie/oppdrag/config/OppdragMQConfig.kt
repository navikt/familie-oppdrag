package no.nav.familie.oppdrag.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import com.ibm.mq.jms.MQQueueConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import javax.jms.ConnectionFactory
import javax.jms.JMSException
import org.springframework.jms.core.JmsTemplate


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
        val mqQueueConnectionFactory = MQQueueConnectionFactory()
        mqQueueConnectionFactory.hostName = hostname
        mqQueueConnectionFactory.queueManager = queuemanager
        mqQueueConnectionFactory.channel = channel
        mqQueueConnectionFactory.port = port

        val cf = UserCredentialsConnectionFactoryAdapter()
        cf.setUsername(user)
        cf.setPassword(password)
        cf.setTargetConnectionFactory(mqQueueConnectionFactory)
        return cf
    }

    @Bean
    fun jmsTemplate(connectionFactory: ConnectionFactory): JmsTemplate {
        val jmsTemplate = JmsTemplate(connectionFactory)
        jmsTemplate.defaultDestinationName = queuename
        return jmsTemplate
    }
}