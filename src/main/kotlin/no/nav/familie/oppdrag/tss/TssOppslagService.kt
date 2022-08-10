package no.nav.familie.oppdrag.tss

import no.rtv.namespacetss.SamhandlerIDataB910Type
import no.rtv.namespacetss.SamhandlerIDataB985Type
import no.rtv.namespacetss.TssSamhandlerData
import no.rtv.namespacetss.TypeOD910
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TssOppslagService(private val tssMQClient: TssMQClient) {
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun hentSamhandlerDataForOrganisasjonB910(orgNr: String): TypeOD910 {
        val samhandlerData = tssMQClient.getOrgInfo(orgNr)
        validateB910response(orgNr, samhandlerData)
        return samhandlerData.tssOutputData.samhandlerODataB910
    }

    fun hentSamhandlerDataForOrganisasjon(orgNr: String): SamhandlerInfo {
        val samhandlerData910 = hentSamhandlerDataForOrganisasjonB910(orgNr)

        val enkeltSamhandler = samhandlerData910.enkeltSamhandler.first()
        val navn = enkeltSamhandler.samhandler110.samhandler.first().navnSamh
        val tssId = enkeltSamhandler.samhandlerAvd125.samhAvd.filter { it.kilde == "IT00" }.first().idOffTSS
        val avdNr = enkeltSamhandler.samhandlerAvd125.samhAvd.filter { it.kilde == "IT00" }.first().avdNr

        val avdelingsAdresser = enkeltSamhandler.adresse130.adresseSamh.filter {
            it.avdNr == avdNr && it.gyldigAdresse == "J" }
            .map {
                it.adrLinjeInfo.adresseLinje
                SamhandlerAddresse(it.adrLinjeInfo.adresseLinje, it.postNr, it.poststed, it.beskrAdresseType)
            }
        return SamhandlerInfo(tssId, navn, avdelingsAdresser)
    }

    fun hentInformasjonOmSamhandlerInst(navn: String): TssSamhandlerData {
        val samhandlerData = tssMQClient.søkOrgInfo(navn)
//        validateB910response(navn, samhandlerData)
        return samhandlerData
    }
    private fun validateB910response(inputData: String, tssResponse: TssSamhandlerData) {
        commonResponseValidation(tssResponse)
        val svarStatus = tssResponse.tssOutputData.svarStatus
        if (svarStatus.alvorligGrad != TSS_STATUS_OK) {
            if (svarStatus.kodeMelding == TSS_KODEMELDING_INGEN_FUNNET) {
                throw TssNoDataFoundException("Ingen treff med med inputData=$inputData")
            }
            throw TssResponseException(svarStatus.beskrMelding, svarStatus.alvorligGrad, svarStatus.kodeMelding)
        }
        if (tssResponse.tssOutputData.ingenReturData != null) {
            throw TssNoDataFoundException("Ingen returdata for TSS request med inputData=$inputData")
        }
    }

    private fun commonResponseValidation(tssResponse: TssSamhandlerData) {
        if (tssResponse.tssOutputData == null || tssResponse.tssOutputData.svarStatus == null || tssResponse.tssOutputData.svarStatus.alvorligGrad == null) {
            throw TssConnectionException("Ingen response. Mest sannsynlig timeout mot TSS")
        }
    }

    companion object {
        const val TSS_KODEMELDING_INGEN_FUNNET = "B9XX008F"
        const val TSS_STATUS_OK = "00"
    }
}
