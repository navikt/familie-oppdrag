package no.nav.familie.oppdrag.tss

import io.swagger.v3.oas.annotations.Operation
import no.nav.familie.kontrakter.ba.tss.SamhandlerInfo
import no.nav.familie.kontrakter.ba.tss.SøkSamhandlerInfo
import no.nav.familie.kontrakter.ba.tss.SøkSamhandlerInfoRequest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.rtv.namespacetss.TOutputElementer
import no.rtv.namespacetss.TypeOD910
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tss")
@ProtectedWithClaims(issuer = "azuread")
class TssController(private val tssOppslagService: TssOppslagService) {

    @Operation(summary = "Henter informasjon om samhandler ved bruk av ORGNR og TSS-tjensten B910")
    @PostMapping(path = ["/proxy/b910/{orgnr}"])
    fun hentSamhandlerDataForOrganisasjonProxy(
        @PathVariable("orgnr") orgnr: String
    ): Ressurs<TypeOD910> {
        return Ressurs.success(tssOppslagService.hentSamhandlerDataForOrganisasjonB910(orgnr))
    }

    @Operation(summary = "Søk informasjon samhandlere av type INST ved bruk av navn og TSS-tjensten B940 og TSS-tjensten B940. Returnerer TSS-output data i rå format")
    @PostMapping(path = ["/proxy/b940"])
    fun søkSamhnadlerinfoFraNavnProxy(
        @RequestBody request: SøkSamhandlerInfoRequest
    ): Ressurs<TOutputElementer> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandlerInstB940(request.navn, request.side).tssOutputData)
    }

    @Operation(summary = "Henter informasjon om samhandler ved bruk av ORGNR og TSS-tjensten B910")
    @PostMapping(path = ["/orgnr/{orgnr}"])
    fun hentSamhandlerDataForOrganisasjon(
        @PathVariable("orgnr") orgnr: String
    ): Ressurs<SamhandlerInfo> {
        return Ressurs.success(tssOppslagService.hentSamhandlerDataForOrganisasjon(orgnr))
    }

    @Operation(summary = "Søk samhandlere ved bruk av navn og TSS-tjensten B940. Første side er 0")
    @PostMapping(path = ["/navn"])
    fun søkSamhnadlerinfoFraNavn(
        @RequestBody requst: SøkSamhandlerInfoRequest
    ): Ressurs<SøkSamhandlerInfo> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandlerInst(requst.navn, requst.side))
    }
}
