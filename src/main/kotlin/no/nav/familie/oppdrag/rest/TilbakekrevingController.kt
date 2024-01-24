package no.nav.familie.oppdrag.rest

import jakarta.validation.Valid
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.oppdrag.tilbakekreving.ØkonomiClient
import no.nav.okonomi.tilbakekrevingservice.KravgrunnlagAnnulerRequest
import no.nav.okonomi.tilbakekrevingservice.KravgrunnlagAnnulerResponse
import no.nav.okonomi.tilbakekrevingservice.KravgrunnlagHentDetaljRequest
import no.nav.okonomi.tilbakekrevingservice.KravgrunnlagHentDetaljResponse
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigInteger
import java.util.UUID

@RestController
@RequestMapping("/api/tilbakekreving")
@ProtectedWithClaims(issuer = "azuread")
class TilbakekrevingController(private val økonomiClient: ØkonomiClient) {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/iverksett/{behandlingId}"])
    fun iverksettVedtak(
        @PathVariable("behandlingId") behandlingId: UUID,
        @Valid @RequestBody
        tilbakekrevingsvedtakRequest: TilbakekrevingsvedtakRequest,
    ): Ressurs<TilbakekrevingsvedtakResponse> {
        return Ressurs.success(økonomiClient.iverksettVedtak(behandlingId, tilbakekrevingsvedtakRequest))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/kravgrunnlag/{kravgrunnlagId}"])
    fun hentKravgrunnlag(
        @PathVariable("kravgrunnlagId") kravgrunnlagId: BigInteger,
        @Valid @RequestBody
        hentKravgrunnlagRequest: KravgrunnlagHentDetaljRequest,
    ): Ressurs<KravgrunnlagHentDetaljResponse> {
        return Ressurs.success(økonomiClient.hentKravgrunnlag(kravgrunnlagId, hentKravgrunnlagRequest))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/annuler/kravgrunnlag/{kravgrunnlagId}"])
    fun annulerKravgrunnlag(
        @PathVariable("kravgrunnlagId") kravgrunnlagId: BigInteger,
        @Valid @RequestBody
        kravgrunnlagAnnulerRequest: KravgrunnlagAnnulerRequest,
    ): Ressurs<KravgrunnlagAnnulerResponse> {
        return Ressurs.success(økonomiClient.annulerKravgrunnlag(kravgrunnlagId, kravgrunnlagAnnulerRequest))
    }
}
