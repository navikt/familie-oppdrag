package no.nav.familie.oppdrag.tss

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jms.core.JmsTemplate
import javax.jms.Message

internal class TssOppslagServiceTest {

    private val mqClient: TssMQClient = mockk()

    private val jmsTemplate: JmsTemplate = mockk(relaxed = false)

    private val mockedMessage: Message = mockk()

    lateinit var service: TssOppslagService

    @BeforeEach
    fun setUp() {
        service = TssOppslagService(TssMQClient(jmsTemplate))
        every { jmsTemplate.defaultDestinationName } returns "mockketKø"
        every { jmsTemplate.connectionFactory } returns mockk(relaxed = true)
        every { jmsTemplate.sendAndReceive(any()) } returns mockedMessage
    }

    @Test
    fun b910() {

        every { mockedMessage.getBody(String::class.java) } returns lesFil("1")
        service.hentSamhandlerDataForOrganisasjon("8003424215")
    }

    private fun lesFil(fileName: String): String {
//        val res = b910
//
//        val file = res.file
//        return file.readText(Charsets.UTF_8)

        return TssOppslagServiceTest::class.java.getResource("/tss-910-response.xml").readText()
    }
}
