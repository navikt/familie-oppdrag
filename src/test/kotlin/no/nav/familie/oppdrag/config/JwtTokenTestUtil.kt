package no.nav.familie.oppdrag.config

import no.nav.security.mock.oauth2.MockOAuth2Server

object JwtTokenTestUtil {
    fun lagAzureAdToken(
        mockOAuth2Server: MockOAuth2Server,
        subject: String = "test-user@nav.no",
        audience: String = "aud-localhost",
        groups: List<String> = emptyList(),
        tilleggsClaims: Map<String, Any> = emptyMap(),
    ): String =
        mockOAuth2Server
            .issueToken(
                issuerId = "azuread",
                subject = subject,
                audience = audience,
                claims = mapOf("groups" to groups, "preferred_username" to subject) + tilleggsClaims,
            ).serialize()

    fun lagUtgaattToken(
        mockOAuth2Server: MockOAuth2Server,
        subject: String = "test-user@nav.no",
        audience: String = "aud-localhost",
    ): String =
        mockOAuth2Server
            .issueToken(
                issuerId = "azuread",
                subject = subject,
                audience = audience,
                expiry = -3600, // token utløpt for 1 time siden
            ).serialize()

    fun lagTokenMedFeilIssuer(
        mockOAuth2Server: MockOAuth2Server,
        subject: String = "test-user@nav.no",
        audience: String = "aud-localhost",
    ): String =
        mockOAuth2Server
            .issueToken(
                issuerId = "wrong-issuer",
                subject = subject,
                audience = audience,
            ).serialize()

    fun lagTokenMedFeilAudience(
        mockOAuth2Server: MockOAuth2Server,
        subject: String = "test-user@nav.no",
    ): String =
        mockOAuth2Server
            .issueToken(
                issuerId = "azuread",
                subject = subject,
                audience = "wrong-audience",
            ).serialize()
}
