package no.nav.familie.oppdrag.grensesnittavstemming

import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Avstemmingsdata
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.stream.XMLInputFactory

class JaxbAvstemmingsdata {

    val jaxbContext = JAXBContext.newInstance(Avstemmingsdata::class.java)
    val marshaller = jaxbContext.createMarshaller().apply {
        setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    }
    val unmarshaller = jaxbContext.createUnmarshaller()
    val xmlInputFactory = XMLInputFactory.newInstance()

    fun tilXml(avstemmingsmelding: Avstemmingsdata) : String {
        val stringWriter = StringWriter()
        marshaller.marshal(avstemmingsmelding, stringWriter)
        return stringWriter.toString()
    }

}