package no.nav.familie.oppdrag.konsistensavstemming

import no.nav.virksomhet.tjenester.avstemming.informasjon.konsistensavstemmingsdata.v1.SendAsynkronKonsistensavstemmingsdataRequest
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

object JaxbKonsistensavstemming {

    val jaxbContext = JAXBContext.newInstance(SendAsynkronKonsistensavstemmingsdataRequest::class.java)
    val marshaller = jaxbContext.createMarshaller().apply {
        setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    }

    fun tilXml(konsistensavstemmingRequest: SendAsynkronKonsistensavstemmingsdataRequest): String {
        val stringWriter = StringWriter()
        marshaller.marshal(konsistensavstemmingRequest, stringWriter)
        return stringWriter.toString()
    }
}