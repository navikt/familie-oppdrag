package no.nav.familie.oppdrag.tss

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jms.core.JmsTemplate
import javax.jms.Message
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun `Skal hente samhandlerinfo for orgnr `() {
        every { mockedMessage.getBody(String::class.java) } returns lesFil("tss-910-response.xml")
        val response = service.hentSamhandlerDataForOrganisasjon("ORGNR")
        assertEquals("Inst 1", response.navn)
        assertEquals("80000112244", response.tssEksternId)
        assertEquals(1, response.adresser.size)
        assertEquals("0550", response.adresser.first().postNr)
        assertEquals("Oslo", response.adresser.first().postSted)
        assertEquals("Arbeidsadresse", response.adresser.first().adresseType)
        assertEquals("Vei 3", response.adresser.first().adresselinjer.first())
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

    @Test
    fun `Skal hente samhandlerinfo ved søk på navn ved bruk av proxytjenesten b940`() {
        every { mockedMessage.getBody(String::class.java) } returns lesFil("tss-940-response.xml")
        val response = service.hentInformasjonOmSamhandlerInstB940(navn = "Inst", postNummer = null, område = null, side = 0).tssOutputData.samhandlerODataB940
        assertEquals(3, response.enkeltSamhandler.size)
        assertEquals("2", response.enkeltSamhandler.first().samhandlerAvd125.antSamhAvd)
        assertEquals("80000442211", response.enkeltSamhandler.first().samhandlerAvd125.samhAvd.filter { it.kilde == "IT00" }.first().idOffTSS)
        assertEquals("80000112244", response.enkeltSamhandler.get(1).samhandlerAvd125.samhAvd.filter { it.kilde == "IT00" }.first().idOffTSS)
        assertEquals(0, response.enkeltSamhandler.get(2).samhandlerAvd125.samhAvd.filter { it.kilde == "IT00" }.size)
    }

    @Test
    fun `Skal søke samhandlerinfo fra navn returnere false hvis det ikke er flere sider`() {
        every { mockedMessage.getBody(String::class.java) } returns lesFil("tss-940-response.xml")
        val response = service.hentInformasjonOmSamhandlerInst("ORGNR", postNummer = null, område = null, side = 0)
        assertFalse { response.finnesMerInfo }
        assertEquals(2, response.samhandlere.size)
        assertEquals("Inst 1", response.samhandlere.first().navn)
        assertEquals("80000442211", response.samhandlere.first().tssEksternId)
        assertEquals(1, response.samhandlere.first().adresser.size)
        assertEquals("0550", response.samhandlere.first().adresser.first().postNr)
        assertEquals("Oslo", response.samhandlere.first().adresser.first().postSted)
        assertEquals("Arbeidsadresse", response.samhandlere.first().adresser.first().adresseType)
        assertEquals("[Vei 3]", response.samhandlere.first().adresser.first().adresselinjer.toString())
        assertEquals("Inst 2", response.samhandlere.last().navn)
        assertEquals("80000112244", response.samhandlere.last().tssEksternId)
    }

    @Test
    fun `Skal søke samhandlerinfo fra navn returnere true hvis det er flere treff`() {
        every { mockedMessage.getBody(String::class.java) } returns lesFil("tss-940-mer-info-response.xml")
        val response = service.hentInformasjonOmSamhandlerInst("ORGNR", postNummer = null, område = null, side = 0)
        assertTrue { response.finnesMerInfo }
        assertEquals(2, response.samhandlere.size)
        assertEquals("Inst 1", response.samhandlere.first().navn)
        assertEquals("80000442211", response.samhandlere.first().tssEksternId)
        assertEquals(1, response.samhandlere.first().adresser.size)
        assertEquals("0550", response.samhandlere.first().adresser.first().postNr)
        assertEquals("Oslo", response.samhandlere.first().adresser.first().postSted)
        assertEquals("Arbeidsadresse", response.samhandlere.first().adresser.first().adresseType)
        assertEquals("[Vei 3]", response.samhandlere.first().adresser.first().adresselinjer.toString())
        assertEquals("Inst 2", response.samhandlere.last().navn)
        assertEquals("80000112244", response.samhandlere.last().tssEksternId)
    }

    @Test
    fun `Skal søke samhandlerinfo fra navn returnere false hvis det ikke er flere treff`() {
        every { mockedMessage.getBody(String::class.java) } returns lesFil("tss-940-ikke-mer-info-response.xml")
        val response = service.hentInformasjonOmSamhandlerInst("ORGNR", postNummer = null, område = null, side = 0)
        assertFalse { response.finnesMerInfo }
        assertEquals(2, response.samhandlere.size)
        assertEquals("Inst 1", response.samhandlere.first().navn)
        assertEquals("80000442211", response.samhandlere.first().tssEksternId)
        assertEquals(1, response.samhandlere.first().adresser.size)
        assertEquals("0550", response.samhandlere.first().adresser.first().postNr)
        assertEquals("Oslo", response.samhandlere.first().adresser.first().postSted)
        assertEquals("Arbeidsadresse", response.samhandlere.first().adresser.first().adresseType)
        assertEquals("[Vei 3]", response.samhandlere.first().adresser.first().adresselinjer.toString())
        assertEquals("Inst 2", response.samhandlere.last().navn)
        assertEquals("80000112244", response.samhandlere.last().tssEksternId)
    }

    private fun lesFil(fileName: String): String {
        return TssOppslagServiceTest::class.java.getResource("/tss/$fileName").readText()
    }
}
