package no.nav.familie.oppdrag.tss

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tss")
@ProtectedWithClaims(issuer = "azuread")
class TssController(private val tssOppslagService: TssOppslagService) {

    @PostMapping(path = ["/b910/{orgnr}"])
    @Unprotected
    fun b910(
        @PathVariable("orgnr") orgnr: String
    ): Ressurs<String> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandler(orgnr))
    }

    @PostMapping(path = ["/b940/{navn}"])
    @Unprotected
    fun b940(
        @PathVariable("navn") navn: String
    ): Ressurs<String> {
        return Ressurs.success(tssOppslagService.hentInformasjonOmSamhandlerInst(navn))
    }
}
