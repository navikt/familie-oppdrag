package no.nav.familie.oppdrag.konsistensavstemming

import no.nav.virksomhet.tjenester.avstemming.v1.SendAsynkronKonsistensavstemmingsdata
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

object JaxbKonsistensavstemming {

    val jaxbContext = JAXBContext.newInstance(SendAsynkronKonsistensavstemmingsdata::class.java)

    fun tilXml(konsistensavstemmingRequest: SendAsynkronKonsistensavstemmingsdata): String {
        val marshaller = jaxbContext.createMarshaller().apply {
            setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        }
        val stringWriter = StringWriter()
        marshaller.marshal(konsistensavstemmingRequest, stringWriter)
        return stringWriter.toString()
    }
}