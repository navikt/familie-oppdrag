package no.nav.familie.oppdrag.iverksetting

import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import java.io.StringReader
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.stream.XMLInputFactory
import javax.xml.transform.stream.StreamSource

object Jaxb {

    val jaxbContext = JAXBContext.newInstance(Oppdrag::class.java,
                                              SimulerBeregningRequest::class.java,
                                              SimulerBeregningResponse::class.java)
    val xmlInputFactory = XMLInputFactory.newInstance()

    fun tilOppdrag(oppdragXml: String): Oppdrag {
        val oppdrag = jaxbContext.createUnmarshaller().unmarshal(
                xmlInputFactory.createXMLStreamReader(StreamSource(StringReader(oppdragXml))),
                Oppdrag::class.java
        )

        return oppdrag.value
    }

    fun tilXml(oppdrag: Oppdrag): String {
        val stringWriter = StringWriter()
        val marshaller = jaxbContext.createMarshaller().apply {
            setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        }
        marshaller.marshal(oppdrag, stringWriter)
        return stringWriter.toString()
    }

    fun tilXml(request: SimulerBeregningRequest): String {
        val stringWriter = StringWriter()
        val marshaller = jaxbContext.createMarshaller().apply {
            setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        }
        marshaller.marshal(request, stringWriter)
        return stringWriter.toString()
    }

    fun tilXml(response: SimulerBeregningResponse): String {
        val stringWriter = StringWriter()
        val marshaller = jaxbContext.createMarshaller().apply {
            setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        }
        marshaller.marshal(response, stringWriter)
        return stringWriter.toString()
    }
}
