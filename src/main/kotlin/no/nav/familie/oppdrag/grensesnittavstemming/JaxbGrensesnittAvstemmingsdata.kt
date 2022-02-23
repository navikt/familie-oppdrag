package no.nav.familie.oppdrag.grensesnittavstemming

import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Avstemmingsdata
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

object JaxbGrensesnittAvstemmingsdata {

    val jaxbContext = JAXBContext.newInstance(Avstemmingsdata::class.java)

    fun tilXml(avstemmingsmelding: Avstemmingsdata): String {
        val stringWriter = StringWriter()
        jaxbContext.createMarshaller().apply {
            setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        }.marshal(avstemmingsmelding, stringWriter)
        return stringWriter.toString()
    }

}