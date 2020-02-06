package no.nav.familie.oppdrag.iverksetting

import no.trygdeetaten.skjema.oppdrag.Oppdrag
import java.io.StringReader
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.stream.XMLInputFactory
import javax.xml.transform.stream.StreamSource

object Jaxb {

    val jaxbContext = JAXBContext.newInstance(Oppdrag::class.java)
    val marshaller = jaxbContext.createMarshaller().apply {
        setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    }
    val unmarshaller = jaxbContext.createUnmarshaller()
    val xmlInputFactory = XMLInputFactory.newInstance()

    fun tilOppdrag(oppdragXml: String): Oppdrag {
        val oppdrag = unmarshaller.unmarshal(
                xmlInputFactory.createXMLStreamReader(StreamSource(StringReader(oppdragXml))),
                Oppdrag::class.java
        )

        return oppdrag.value
    }

    fun tilXml(oppdrag: Oppdrag): String {
        val stringWriter = StringWriter()
        marshaller.marshal(oppdrag, stringWriter)
        return stringWriter.toString()
    }
}