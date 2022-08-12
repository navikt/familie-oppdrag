package no.nav.familie.oppdrag.tss

import io.swagger.v3.oas.annotations.Operation
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.rtv.namespacetss.TOutputElementer
import no.rtv.namespacetss.TypeOD910
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tss")
@ProtectedWithClaims(issuer = "azuread")
class TssController(private val tssOppslagService: TssOppslagService) {

    @Operation(summary = "Henter informasjon om samhandler ved bruk av ORGNR ved bruk av TSS-tjensten B910")
    @PostMapping(path = ["/proxy/b910/{orgnr}"])
    @Unprotected
    fun hentSamhandlerDataForOrganisasjonProxy(
        @PathVariable("orgnr") orgnr: String
    ): Ressurs<TypeOD910> {
        return Ressurs.success(tssOppslagService.hentSamhandlerDataForOrganisasjonB910(orgnr))
    }

    @Operation(summary = "Henter informasjon om samhandlere av type INST ved søk på navn.  Bruker TSS-tjensten B940")
    @PostMapping(path = ["/proxy/b940/{navn}/{side}"])
    @Unprotected
    fun søkSamhnadlerinfoFraNavnProxy(
        @PathVariable("navn") navn: String,
        @PathVariable("side") side: String = "000"
    ): Ressurs<TOutputElementer> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandlerInstB940(navn, side).tssOutputData)
    }

    @Operation(summary = "Henter informasjon om samhandler ved bruk av ORGNR ved bruk av TSS-tjensten B910")
    @PostMapping(path = ["/orgnr/{orgnr}"])
    @Unprotected
    fun hentSamhandlerDataForOrganisasjon(
        @PathVariable("orgnr") orgnr: String
    ): Ressurs<SamhandlerInfo> {
        return Ressurs.success(tssOppslagService.hentSamhandlerDataForOrganisasjon(orgnr))
    }

    @Operation(summary = "Henter informasjon om samhandler ved bruk av ORGNR ved bruk av TSS-tjensten B910")
    @PostMapping(path = ["/navn/{navn}/{side}"])
    @Unprotected
    fun søkSamhnadlerinfoFraNavn(
        @PathVariable("navn") navn: String,
        @PathVariable("side") side: String = "000"
    ): Ressurs<SøkSamhandlerInfo> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandlerInst(navn, side))
    }
}

data class SøkSamhandlerInfo(
    val finnesMerInfo: Boolean,
    val samhandlere: List<SamhandlerInfo>
)

data class SamhandlerInfo(
    val tssEksternId: String,
    val navn: String,
    val adressser: List<SamhandlerAddresse> = emptyList()
)

data class SamhandlerAddresse(
    val adresselinjer: List<String>,
    val postNr: String,
    val postSted: String,
    val addresseType: String

)
