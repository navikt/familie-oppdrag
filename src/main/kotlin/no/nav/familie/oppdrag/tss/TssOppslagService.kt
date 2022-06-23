package no.nav.familie.oppdrag.tss

import no.nav.familie.oppdrag.iverksetting.Jaxb
import no.rtv.namespacetss.ObjectFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TssOppslagService(private val tssMQClient: TssMQClient) {
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun hentInformasjonOmSamhandler(orgNr: String): String {
        val objectFactory = ObjectFactory()
        val servicerutiner = objectFactory.createTServicerutiner()

        val samhandlerIDataB910 = objectFactory.createSamhandlerIDataB910Type()

        samhandlerIDataB910.brukerID = "HMB2990"
        samhandlerIDataB910.historikk = "N"

        val tidOFF1 = objectFactory.createTidOFF1()
        tidOFF1.idOff = orgNr
        tidOFF1.kodeIdType = "ORG"
        tidOFF1.kodeSamhType = "INST"
        samhandlerIDataB910.ofFid = tidOFF1

        servicerutiner.samhandlerIDataB910 = samhandlerIDataB910

        val tssInputData = objectFactory.createTssSamhandlerDataTssInputData()
        tssInputData.tssServiceRutine = servicerutiner
        val tssSamhandlerData = objectFactory.createTssSamhandlerData()
        tssSamhandlerData.tssInputData = tssInputData

        val response = tssMQClient.kallTss(Jaxb.tilXml(tssSamhandlerData))
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
}
