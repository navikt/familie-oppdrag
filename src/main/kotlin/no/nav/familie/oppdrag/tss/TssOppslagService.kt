package no.nav.familie.oppdrag.tss

import no.rtv.namespacetss.SamhandlerIDataB985Type
import no.rtv.namespacetss.TssSamhandlerData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TssOppslagService(private val tssMQClient: TssMQClient) {
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun hentInformasjonOmSamhandler(orgNr: String): TssSamhandlerData {
        val samhandlerData = tssMQClient.getOrgInfo(orgNr)
        return samhandlerData
    }

    fun hentInformasjonOmSamhandlerInst(navn: String): TssSamhandlerData {
        val samhandlerData = tssMQClient.søkOrgInfo(navn)
        return samhandlerData
    }

    fun hentInformasjonOmSamhandlerInstB85(request: SamhandlerIDataB985Type): TssSamhandlerData {
        val samhandlerData = tssMQClient.søkOrgInfoB985(request)
        return samhandlerData
    }
    private fun validateB910Response(tssEksternId: String, tssResponse: TssSamhandlerData) {
//        commonResponseValidation(tssResponse)
        val svarStatus = tssResponse.tssOutputData.svarStatus
        val status = svarStatus.alvorligGrad
        val statusOk = "00"
        if (statusOk != status) {
            throw TssResponseException(svarStatus.alvorligGrad, svarStatus.kodeMelding, svarStatus.beskrMelding)
        }
//        if (tssResponse.tssOutputData.ingenReturData != null) {
//            throw TssNoDataFoundException(tssEksternId, tssResponse.tssOutputData.ingenReturData.toString())
//        }
    }

//    private fun commonResponseValidation(tssResponse: TssSamhandlerData?) {
//        if (tssResponse == null || tssResponse.tssOutputData == null || tssResponse.tssOutputData.svarStatus == null || tssResponse.tssOutputData.svarStatus.alvorligGrad == null) {
//            throw TssConnectionException(TSS_COMMON_RESPONSE_VALIDATION)
//        }
//    }
}
