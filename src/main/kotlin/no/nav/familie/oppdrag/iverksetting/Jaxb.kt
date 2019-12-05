package no.nav.familie.oppdrag.iverksetting

import no.trygdeetaten.skjema.oppdrag.Oppdrag
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

class Jaxb {

    val marshaller = JAXBContext.newInstance(Oppdrag::class.java).createMarshaller().apply {
        setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    }

    fun tilXml(oppdrag: Oppdrag): String {
        val stringWriter = StringWriter()
        marshaller.marshal(oppdrag, stringWriter)
        return stringWriter.toString()
    }
}