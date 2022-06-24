package no.nav.familie.oppdrag.config

import com.ibm.mq.constants.CMQC.MQENC_NATIVE
import com.ibm.mq.jms.MQQueueConnectionFactory
import com.ibm.msg.client.jms.JmsConstants
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING
import com.ibm.msg.client.jms.JmsFactoryFactory
import com.ibm.msg.client.wmq.WMQConstants
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT
import org.apache.activemq.jms.pool.PooledConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.config.JmsListenerContainerFactory
import org.springframework.jms.connection.JmsTransactionManager
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate
import javax.jms.ConnectionFactory
import javax.jms.JMSException

private const val UTF_8_WITH_PUA = 1208

@Configuration
class OppdragMQConfig(
    @Value("\${oppdrag.mq.hostname}") val hostname: String,
    @Value("\${oppdrag.mq.queuemanager}") val queuemanager: String,
    @Value("\${oppdrag.mq.channel}") val channel: String,
    @Value("\${oppdrag.mq.send}") val sendQueue: String,
    @Value("\${oppdrag.mq.avstemming}") val avstemmingQueue: String,
    @Value("\${oppdrag.mq.tss}") val tssQueue: String,
    @Value("\${oppdrag.mq.port}") val port: Int,
    @Value("\${oppdrag.mq.user}") val user: String,
    @Value("\${oppdrag.mq.password}") val password: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Bean
    @Throws(JMSException::class)
    fun mqQueueConnectionFactory(): PooledConnectionFactory {
        val targetFactory = MQQueueConnectionFactory()
        targetFactory.hostName = hostname
        targetFactory.queueManager = queuemanager
        targetFactory.channel = channel
        targetFactory.port = port
        targetFactory.transportType = WMQ_CM_CLIENT
        targetFactory.ccsid = UTF_8_WITH_PUA
        targetFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE)
        targetFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, true)
        targetFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA)

        val cf = UserCredentialsConnectionFactoryAdapter()
        cf.setUsername(user)
        cf.setPassword(password)
        cf.setTargetConnectionFactory(targetFactory)

        val pooledFactory = PooledConnectionFactory()
        pooledFactory.connectionFactory = cf
        pooledFactory.maxConnections = 10
        pooledFactory.maximumActiveSessionPerConnection = 10

        return pooledFactory
    }

    @Bean
    fun jmsTemplateUtgående(mqQueueConnectionFactory: ConnectionFactory): JmsTemplate {
        return JmsTemplate(mqQueueConnectionFactory).apply {
            defaultDestinationName = sendQueue
            isSessionTransacted = true
        }
    }

    @Bean
    fun tssConnectionFactory(): ConnectionFactory {
        val ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER)
        val cf = ff.createConnectionFactory()
        // Set the properties
        cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, hostname)
        cf.setIntProperty(WMQConstants.WMQ_PORT, port)
        cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel)
        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQ_CM_CLIENT)
        cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queuemanager)
        cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "familie-oppdrag")
        cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true)
        cf.setStringProperty(WMQConstants.USERID, user)
        cf.setStringProperty(WMQConstants.PASSWORD, password)
        return cf
    }

    @Bean
    fun jmsTemplateTss(
        @Qualifier("tssConnectionFactory") tssConnectionFactory: ConnectionFactory
    ): JmsTemplate {

        return JmsTemplate(tssConnectionFactory).apply {
            defaultDestinationName = tssQueue
            isSessionTransacted = true
        }
    }

    @Bean fun jmsListenerContainerFactory(
        mqQueueConnectionFactory: ConnectionFactory,
        configurer: DefaultJmsListenerContainerFactoryConfigurer
    ): JmsListenerContainerFactory<*> {
        val factory = DefaultJmsListenerContainerFactory()
        configurer.configure(factory, mqQueueConnectionFactory)

        val transactionManager = JmsTransactionManager()
        transactionManager.connectionFactory = mqQueueConnectionFactory
        factory.setTransactionManager(transactionManager)
        factory.setSessionTransacted(true)
        factory.setErrorHandler {
            logger.error("Feilet håndtering av melding, se secureLogs")
            secureLogger.error("Feilet håndtering av melding", it)
        }
        factory.setExceptionListener {
            logger.error("Feilet lytting av kø, se secureLogs")
            secureLogger.error("Feilet lytting av kø", it)
        }
        return factory
    }

    @Bean
    fun jmsTemplateAvstemming(mqQueueConnectionFactory: ConnectionFactory): JmsTemplate {
        return JmsTemplate(mqQueueConnectionFactory).apply { defaultDestinationName = avstemmingQueue }
    }
}
