package no.nav.familie.oppdrag.tss

import io.swagger.v3.oas.annotations.Operation
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.rtv.namespacetss.SamhandlerIDataB985Type
import no.rtv.namespacetss.TOutputElementer
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tss")
@ProtectedWithClaims(issuer = "azuread")
class TssController(private val tssOppslagService: TssOppslagService) {


    @Operation(summary = "Henter informasjon om samhandler ved bruk av ORGNR ved bruk av TSS-tjensten B910")
    @PostMapping(path = ["/proxy/b910/{orgnr}"])
    @Unprotected
    fun hentSamhandlerDataForOrganisasjon(
        @PathVariable("orgnr") orgnr: String
    ): Ressurs<TOutputElementer> {
        return Ressurs.success(tssOppslagService.hentSamhandlerDataForOrganisasjon(orgnr).tssOutputData)
    }

    @Operation(summary = "Henter informasjon om samhandlere av type INST ved søk på navn.  Bruker TSS-tjensten B940")
    @PostMapping(path = ["/proxy/b940/{navn}"])
    @Unprotected
    fun b940(
        @PathVariable("navn") navn: String
    ): Ressurs<TOutputElementer> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandlerInst(navn).tssOutputData)
    }

    @Operation(summary = "Henter komplett informasjon om samhandlere ved søk på navn. Bruker TSS-tjenesten B960")
    @PostMapping(path = ["/proxy/b960/{orgnr}"])
    @Unprotected
    fun b960(
        @PathVariable("orgnr") orgnr: String
    ): Ressurs<TOutputElementer> {
        return Ressurs.success(tssOppslagService.hentKomplettSamhandlerInfo(orgnr).tssOutputData)
    }

    @Operation(summary = "Henter informasjon om samhandlere ved søk på navn. Bruker TSS-tjenesten B985")
    @PostMapping(path = ["/proxy/b985"])
    @Unprotected
    fun b985(
        @RequestBody request: SamhandlerIDataB985Type
    ): Ressurs<TOutputElementer> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandlerB85(request).tssOutputData)
    }
}
