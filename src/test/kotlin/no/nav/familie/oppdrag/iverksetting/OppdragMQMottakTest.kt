package no.nav.familie.oppdrag.iverksetting

import com.ibm.mq.jms.MQConnectionFactory
import com.ibm.msg.client.wmq.WMQConstants
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.env.Environment
import java.io.File
import javax.jms.TextMessage
import kotlin.test.assertEquals

class OppdragMQMottakTest {

    var mqConn = MQConnectionFactory().apply {
        hostName = "localhost"
        port = 1414
        channel = "DEV.ADMIN.SVRCONN"
        queueManager = "QM1"
        transportType = WMQConstants.WMQ_CM_CLIENT
    }.createConnection("admin", "passw0rd")

    val session = mqConn.createSession()
    lateinit var oppdragMottaker: OppdragMottaker

    @BeforeEach
    fun setUp() {
        val env = mockk<Environment>()
        every { env.activeProfiles } returns emptyArray()

        oppdragMottaker = OppdragMottaker(env)
    }


    @Test
    fun skal_tolke_kvittering_riktig_ved_OK() {
        val kvittering: TextMessage = lesKvittering("kvittering-akseptert.xml")
        val statusFraKvittering = oppdragMottaker.mottaKvitteringFraOppdrag(kvittering)
        assertEquals(Status.OK, statusFraKvittering)
    }

    @Test
    fun skal_tolke_kvittering_riktig_ved_feil() {
        val kvittering: TextMessage = lesKvittering("kvittering-avvist.xml")
        val statusFraKvittering = oppdragMottaker.mottaKvitteringFraOppdrag(kvittering)
        assertEquals(Status.AVVIST_FUNKSJONELLE_FEIL, statusFraKvittering)
    }

    private fun lesKvittering(filnavn: String): TextMessage {
        val kvittering = File("src/test/resources/$filnavn").inputStream().readBytes().toString(Charsets.UTF_8)

        return session.createTextMessage(kvittering)
    }
}