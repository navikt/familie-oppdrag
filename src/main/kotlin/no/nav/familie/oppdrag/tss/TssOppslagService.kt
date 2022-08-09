package no.nav.familie.oppdrag.tss

import no.rtv.namespacetss.SamhandlerIDataB910Type
import no.rtv.namespacetss.SamhandlerIDataB961Type
import no.rtv.namespacetss.SamhandlerIDataB985Type
import no.rtv.namespacetss.SvarStatusType
import no.rtv.namespacetss.TssSamhandlerData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TssOppslagService(private val tssMQClient: TssMQClient) {
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun hentSamhandlerDataForOrganisasjon(orgNr: String): TssSamhandlerData {
        val samhandlerData = tssMQClient.getOrgInfo(orgNr)
        validateB910response(orgNr, samhandlerData)
        return samhandlerData
    }

    fun hentInformasjonOmSamhandlerInst(navn: String): TssSamhandlerData {
        val samhandlerData = tssMQClient.søkOrgInfo(navn)
        validateB910response(navn, samhandlerData)
        return samhandlerData
    }

    fun hentKomplettSamhandlerInfo(orgNr: String): TssSamhandlerData {
        val samhandlerData = tssMQClient.getKomplettSamhandlerInfo(orgNr)
        validateB910response(orgNr, samhandlerData)
        return samhandlerData
    }

    fun hentInformasjonOmSamhandlerB85(request: SamhandlerIDataB985Type): TssSamhandlerData {
        val samhandlerData = tssMQClient.søkOrgInfoB985(request)
        val inputData= "${request.brukerID} ${request.navnSamh} ${request.delNavn} ${request.kodeSamhType} ${request.buffnr}"
        validateB910response(inputData, samhandlerData)
        return samhandlerData
    }
    private fun validateB910response(inputData: String, tssResponse: TssSamhandlerData) {
        commonResponseValidation(tssResponse)
        val svarStatus = tssResponse.tssOutputData.svarStatus
        val status = svarStatus.alvorligGrad
        val statusOk = "00"
        if (statusOk != status) {
            throw TssResponseException(svarStatus.beskrMelding, svarStatus.alvorligGrad, svarStatus.kodeMelding)
        }
        if (tssResponse.tssOutputData.ingenReturData != null) {
            throw TssNoDataFoundException("Ingen returdata for TSS request med inputData=$inputData")
        }
    }

    private fun commonResponseValidation(tssResponse: TssSamhandlerData) {
        if (tssResponse == null || tssResponse.tssOutputData == null || tssResponse.tssOutputData.svarStatus == null || tssResponse.tssOutputData.svarStatus.alvorligGrad == null) {
            throw TssConnectionException("Ingen response. Mest sannsynlig timeout mot TSS")
        }
    }
}
