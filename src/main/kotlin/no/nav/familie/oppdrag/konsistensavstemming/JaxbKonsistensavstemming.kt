package no.nav.familie.oppdrag.konsistensavstemming

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import no.nav.virksomhet.tjenester.avstemming.v1.SendAsynkronKonsistensavstemmingsdata
import java.io.StringWriter

object JaxbKonsistensavstemming {
    val jaxbContext = JAXBContext.newInstance(SendAsynkronKonsistensavstemmingsdata::class.java)

    fun tilXml(konsistensavstemmingRequest: SendAsynkronKonsistensavstemmingsdata): String {
        val marshaller =
            jaxbContext.createMarshaller().apply {
                setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
            }
        val stringWriter = StringWriter()
        marshaller.marshal(konsistensavstemmingRequest, stringWriter)
        return stringWriter.toString()
    }
}
