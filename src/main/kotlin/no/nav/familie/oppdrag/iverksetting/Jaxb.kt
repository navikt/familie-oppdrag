package no.nav.familie.oppdrag.iverksetting

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import no.rtv.namespacetss.TssSamhandlerData
import no.trygdeetaten.skjema.oppdrag.ObjectFactory
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import java.io.StringReader
import java.io.StringWriter
import javax.xml.stream.XMLInputFactory
import javax.xml.transform.stream.StreamSource

object Jaxb {

    val jaxbContext = JAXBContext.newInstance(
        Oppdrag::class.java,
        SimulerBeregningRequest::class.java,
        SimulerBeregningResponse::class.java,
        TssSamhandlerData::class.java
    )
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
        marshaller.marshal(ObjectFactory().createOppdrag(oppdrag), stringWriter)
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

    fun tilSimuleringsrespons(responsXml: String): SimulerBeregningResponse {
        val simuleringBeregningResponse = jaxbContext.createUnmarshaller().unmarshal(
            xmlInputFactory.createXMLStreamReader(StreamSource(StringReader(responsXml))),
            SimulerBeregningResponse::class.java
        )

        return simuleringBeregningResponse.value
    }

    fun tilXml(request: TssSamhandlerData): String {
        val jaxbContext: JAXBContext = JAXBContext.newInstance(TssSamhandlerData::class.java)
        val marshaller: Marshaller = jaxbContext.createMarshaller()
        val stringWriter = StringWriter()
        marshaller.marshal(request, stringWriter)
        return stringWriter.toString()
    }

    fun tilTssSamhandlerData(responsXml: String): TssSamhandlerData {
        val tssSamhandlerData = jaxbContext.createUnmarshaller().unmarshal(
            xmlInputFactory.createXMLStreamReader(StreamSource(StringReader(responsXml))),
            TssSamhandlerData::class.java
        )

        return tssSamhandlerData.value
    }
}
