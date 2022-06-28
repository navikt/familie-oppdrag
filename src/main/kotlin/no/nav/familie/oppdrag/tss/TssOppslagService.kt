package no.nav.familie.oppdrag.tss

import no.nav.familie.oppdrag.iverksetting.Jaxb
import no.rtv.namespacetss.ObjectFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class TssOppslagService(private val tssMQClient: TssMQClient) {
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Value("classpath:tss/910.xml")
    lateinit var b910: Resource

    fun hentInformasjonOmSamhandler(orgNr: String): String {
        val objectFactory = ObjectFactory()
        val servicerutiner = objectFactory.createTServicerutiner()

        val samhandlerIDataB910 = objectFactory.createSamhandlerIDataB910Type()

        samhandlerIDataB910.brukerID = "HMB2990"
        samhandlerIDataB910.historikk = "N"

        val tidOFF1 = objectFactory.createTidOFF1()
        tidOFF1.idOff = orgNr
        tidOFF1.kodeIdType = "ORG"
        tidOFF1.kodeSamhType = null
        samhandlerIDataB910.ofFid = tidOFF1

        servicerutiner.samhandlerIDataB910 = samhandlerIDataB910

        val tssInputData = objectFactory.createTssSamhandlerDataTssInputData()
        tssInputData.tssServiceRutine = servicerutiner
        val tssSamhandlerData = objectFactory.createTssSamhandlerData()
        tssSamhandlerData.tssInputData = tssInputData
        val xml = Jaxb.tilXml(tssSamhandlerData)

        val xml2 = lesFil("/tss/910.xml").replace("BYTTUT", orgNr)

        secureLogger.info("Sender melding til tss: $xml")
        val response = tssMQClient.kallTss(xml2)
        secureLogger.info("B910 = Response=$response")
        return response
    }

    fun hentInformasjonOmSamhandlerInst(navn: String): String {
        val objectFactory = ObjectFactory()
        val servicerutiner = objectFactory.createTServicerutiner()

        val samhandlerIDataB940 = objectFactory.createSamhandlerIDataB940Type()

        samhandlerIDataB940.brukerID = "HMB2990"
        samhandlerIDataB940.navnSamh = navn

        servicerutiner.samhandlerIDataB940 = samhandlerIDataB940

        val tssInputData = objectFactory.createTssSamhandlerDataTssInputData()
        tssInputData.tssServiceRutine = servicerutiner
        val tssSamhandlerData = objectFactory.createTssSamhandlerData()
        tssSamhandlerData.tssInputData = tssInputData
        val response = tssMQClient.kallTss(Jaxb.tilXml(tssSamhandlerData))
        secureLogger.info("B940 = Response=$response")
        return response
    }

    private fun lesFil(fileName: String): String {
        val res = b910

        val file = res.file
        return file.readText(Charsets.UTF_8)
    }
}
