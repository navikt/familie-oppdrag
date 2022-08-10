package no.nav.familie.oppdrag.tss

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jms.core.JmsTemplate
import javax.jms.Message
import kotlin.test.assertNotNull

internal class TssOppslagServiceTest {

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
    fun `Skal hente samhandlerinfo for orgnr ved bruk av proxytjenesten b910`() {
        every { mockedMessage.getBody(String::class.java) } returns lesFil("tss-910-response.xml")
        val response = service.hentSamhandlerDataForOrganisasjonB910("ORGNR")
        assertEquals(1, response.enkeltSamhandler.size)
        assertEquals("2", response.enkeltSamhandler.first().samhandlerAvd125.antSamhAvd)
        assertEquals("80000112244", response.enkeltSamhandler.first().samhandlerAvd125.samhAvd.filter { it.kilde == "IT00" }.first().idOffTSS)
    }

    @Test
    fun `Skal få TssNoDataFoundException hvis man ikke finner samhandlerinfo for orgnr ved bruk av proxytjenesten`() {
        every { mockedMessage.getBody(String::class.java) } returns lesFil("tss-910-notfound-response.xml")

        val tssNoDataFoundException = assertThrows(TssNoDataFoundException::class.java) {
            service.hentSamhandlerDataForOrganisasjon("ORGNR")
        }

        assertEquals("Ingen treff med med inputData=ORGNR", tssNoDataFoundException.message)
    }

    @Test
    fun `Skal få TssConnectionException hvis jmstemplate returnerer er null pga timeout`() {
        every { jmsTemplate.sendAndReceive(any()) } returns null

        val tssNoDataFoundException = assertThrows(TssConnectionException::class.java) {
            service.hentSamhandlerDataForOrganisasjon("ORGNR")
        }

        assertEquals("En feil oppsto i kallet til TSS. Response var null (timeout?)", tssNoDataFoundException.message)
    }

    @Test
    fun `Skal få TssConnectionException hvis jmstemplate kaster JmsException pga ukjent feil`() {
        every { jmsTemplate.sendAndReceive(any()) } throws RuntimeException("Ukjent feil")

        val tssNoDataFoundException = assertThrows(TssConnectionException::class.java) {
            service.hentSamhandlerDataForOrganisasjon("ORGNR")
        }

        assertEquals("En feil oppsto i kallet til TSS", tssNoDataFoundException.message)
    }

    @Test
    fun `Skal få TssResponseException hvis b910 returerner feil med en annen feilkode enn Ingen treff`() {
        every { mockedMessage.getBody(String::class.java) } returns lesFil("tss-910-error-response.xml")

        val tssResponseException = assertThrows(TssResponseException::class.java) {
            service.hentSamhandlerDataForOrganisasjon("ORGNR")
        }

        assertEquals("DET FINNES MER INFORMASJON-04-B9XX018I", tssResponseException.message)
    }

    private fun lesFil(fileName: String): String {
        return TssOppslagServiceTest::class.java.getResource("/tss/$fileName").readText()
    }
}
