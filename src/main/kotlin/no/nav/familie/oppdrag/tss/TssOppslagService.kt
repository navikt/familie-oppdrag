package no.nav.familie.oppdrag.tss

import no.rtv.namespacetss.Samhandler
import no.rtv.namespacetss.TOutputElementer
import no.rtv.namespacetss.TssSamhandlerData
import no.rtv.namespacetss.TypeKomp940
import no.rtv.namespacetss.TypeOD910
import no.rtv.namespacetss.TypeSamhAdr
import no.rtv.namespacetss.TypeSamhAvd
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
        return mapSamhandler(enkeltSamhandler)
    }



    fun hentInformasjonOmSamhandlerInstB940(navn: String): TOutputElementer.SamhandlerODataB940 {
        val samhandlerData = tssMQClient.søkOrgInfo(navn)
//        validateB910response(navn, samhandlerData)
        return samhandlerData.tssOutputData.samhandlerODataB940
    }

    fun hentInformasjonOmSamhandlerInst(navn: String): List<SamhandlerInfo> {
        val samhandlerODataB940 = hentInformasjonOmSamhandlerInstB940(navn)



//        validateB910response(navn, samhandlerData)
        return samhandlerODataB940.enkeltSamhandler.filter { it.samhandlerAvd125.samhAvd.filter { it.kilde == "IT00" }.isNotEmpty() }.map { mapSamhandler(it) }
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

    private fun mapSamhandler(enkeltSamhandler: Samhandler): SamhandlerInfo {
        val navn = enkeltSamhandler.samhandler110.samhandler.first().navnSamh
        val (tssId, avdNr) = mapTssEksternIdOgAvdNr(enkeltSamhandler.samhandlerAvd125)

        val avdelingsAdresser = madAdresse(enkeltSamhandler.adresse130, avdNr)
        return SamhandlerInfo(tssId, navn, avdelingsAdresser)
    }

    private fun mapSamhandler(enkeltSamhandler: TypeKomp940): SamhandlerInfo {
        val navn = enkeltSamhandler.samhandler110.samhandler.first().navnSamh
        val (tssId, avdNr) = mapTssEksternIdOgAvdNr(enkeltSamhandler.samhandlerAvd125)

        val avdelingsAdresser = madAdresse(enkeltSamhandler.adresse130, avdNr)
        return SamhandlerInfo(tssId, navn, avdelingsAdresser)
    }

    private fun mapTssEksternIdOgAvdNr(samhandlerAvd125: TypeSamhAvd): Pair<String, String> {

        val tssId = samhandlerAvd125.samhAvd.filter { it.kilde == "IT00" }.first().idOffTSS
        val avdNr = samhandlerAvd125.samhAvd.filter { it.kilde == "IT00" }.first().avdNr
        return Pair(tssId, avdNr)
    }

    private fun madAdresse(
        adresse130: TypeSamhAdr,
        avdNr: String?
    ) = adresse130.adresseSamh.filter {
        it.avdNr == avdNr && it.gyldigAdresse == "J"
    }
        .mapNotNull {
            val adresseLinje = if (it.antAdrLinje.isNullOrBlank()) emptyList() else it.adrLinjeInfo.adresseLinje
            SamhandlerAddresse(adresseLinje, it.postNr, it.poststed, it.beskrAdresseType)
        }

    companion object {
        const val TSS_KODEMELDING_INGEN_FUNNET = "B9XX008F"
        const val TSS_STATUS_OK = "00"
    }
}
