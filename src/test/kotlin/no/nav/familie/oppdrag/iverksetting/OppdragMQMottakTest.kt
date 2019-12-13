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

    }

    @Test
    fun skal_tolke_kvittering_riktig_ved_feil() {
        val kvittering: TextMessage = lesKvittering("kvittering-avvist.xml")
        oppdragMottaker.mottaKvitteringFraOppdrag(kvittering)
    }

    @Test
    fun skal_motta_kvittering_når_den_er_lagt_på_kø() {

    }

    private fun lesKvittering(filnavn: String): TextMessage {
        val kvittering = File("src/test/resources/$filnavn").inputStream().readBytes().toString(Charsets.UTF_8)

        return session.createTextMessage(kvittering)
    }
}