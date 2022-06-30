package no.nav.familie.oppdrag.tss

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingRequestV2
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.rtv.namespacetss.SamhandlerIDataB985Type
import no.rtv.namespacetss.TssSamhandlerData
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tss")
@ProtectedWithClaims(issuer = "azuread")
class TssController(private val tssOppslagService: TssOppslagService) {

    @PostMapping(path = ["/b910/{orgnr}"])
    @Unprotected
    fun hentSamhandlerDataForOrganisasjon(
        @PathVariable("orgnr") orgnr: String
    ): Ressurs<TssSamhandlerData> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandler(orgnr))
    }

    @PostMapping(path = ["/b940/{navn}"])
    @Unprotected
    fun b940(
        @PathVariable("navn") navn: String
    ): Ressurs<TssSamhandlerData> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandlerInst(navn))
    }

    @PostMapping(path = ["/b985/{navn}"])
    @Unprotected
    fun b985(
        @RequestBody request: SamhandlerIDataB985Type

    ): Ressurs<TssSamhandlerData> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandlerInstB85(request))
    }
}
