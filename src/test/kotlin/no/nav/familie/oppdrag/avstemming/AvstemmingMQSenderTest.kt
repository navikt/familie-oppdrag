package no.nav.familie.oppdrag.avstemming

import com.ibm.mq.jms.MQConnectionFactory
import com.ibm.msg.client.wmq.WMQConstants
import io.mockk.called
import io.mockk.spyk
import io.mockk.verify
import no.nav.familie.oppdrag.grensesnittavstemming.GrensesnittavstemmingMapper
import no.nav.familie.oppdrag.konsistensavstemming.KonsistensavstemmingMapper
import no.nav.familie.oppdrag.repository.somOppdragLager
import no.nav.familie.oppdrag.util.Containers
import no.nav.familie.oppdrag.util.TestOppdragMedAvstemmingsdato
import no.nav.virksomhet.tjenester.avstemming.informasjon.konsistensavstemmingsdata.v1.Konsistensavstemmingsdata
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Avstemmingsdata
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.lang.UnsupportedOperationException
import java.time.LocalDateTime

private const val TESTKØ = "DEV.QUEUE.2"
private const val FAGOMRÅDE = "BA"
private val IDAG = LocalDateTime.now()

@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
@Testcontainers
@ContextConfiguration(initializers = [Containers.MQInitializer::class])
class AvstemmingMQSenderTest {

    companion object {
        @Container
        var ibmMQContainer = Containers.ibmMQContainer
    }

    private val mqConn = MQConnectionFactory().apply {
        hostName = "localhost"
        port = ibmMQContainer.getMappedPort(1414)
        channel = "DEV.ADMIN.SVRCONN"
        queueManager = "QM1"
        transportType = WMQConstants.WMQ_CM_CLIENT
    }

    private val cf = UserCredentialsConnectionFactoryAdapter().apply {
        setUsername("admin")
        setPassword("passw0rd")
        setTargetConnectionFactory(mqConn)
    }

    private val jmsTemplate = spyk(JmsTemplate(cf).apply { defaultDestinationName = TESTKØ })

    @Test
    fun skal_ikke_sende_avstemming_når_avskrudd() {
        val avstemmingSender = AvstemmingSenderMQ(jmsTemplate, "false")

        Assertions.assertThrows(UnsupportedOperationException::class.java) {
            avstemmingSender.sendKonsistensAvstemming(lagTestKonsistensavstemming()[0])
        }

        verify { jmsTemplate wasNot called }
    }

    @Test
    fun skal_sende_konsistensavstemming_når_påskrudd() {
        val avstemmingSender = AvstemmingSenderMQ(jmsTemplate, "true")

        avstemmingSender.sendKonsistensAvstemming(lagTestKonsistensavstemming()[0])

        verify (exactly = 1) { jmsTemplate.convertAndSend(any<String>(), any<String>()) }
    }

    @Test
    fun skal_sende_grensesnittavstemming_når_påskrudd() {
        val avstemmingSender = AvstemmingSenderMQ(jmsTemplate, "true")

        avstemmingSender.sendGrensesnittAvstemming(lagTestGrensesnittavstemming()[0])

        verify (exactly = 1) { jmsTemplate.convertAndSend(any<String>(), any<String>()) }
    }

    private fun lagTestKonsistensavstemming(): List<Konsistensavstemmingsdata> {
        val utbetalingsoppdrag = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(IDAG, FAGOMRÅDE)
        val mapper = KonsistensavstemmingMapper(FAGOMRÅDE, listOf(utbetalingsoppdrag), IDAG)
        return mapper.lagAvstemmingsmeldinger()
    }

    private fun lagTestGrensesnittavstemming(): List<Avstemmingsdata> {
        val utbetalingsoppdrag = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(IDAG, FAGOMRÅDE)
        val mapper = GrensesnittavstemmingMapper(listOf(utbetalingsoppdrag.somOppdragLager), FAGOMRÅDE, IDAG.minusDays(1), IDAG)
        return mapper.lagAvstemmingsmeldinger()
    }
}