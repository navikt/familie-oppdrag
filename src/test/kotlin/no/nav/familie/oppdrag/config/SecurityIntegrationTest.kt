package no.nav.familie.oppdrag.config

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.FeilutbetalingerFraSimulering
import no.nav.familie.oppdrag.simulering.SimuleringTjeneste
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.client.RestTestClient
import kotlin.test.assertNotEquals

@ActiveProfiles("dev")
@SpringBootTest(
    classes = [SecurityIntegrationTest.TestConfig::class],
    properties = ["spring.cloud.vault.enabled=false"], //TODO se om denne er nødvendig
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(
    initializers = [MockOAuth2ServerInitializer::class],
    classes = [SecurityIntegrationTest.TestConfig::class], //TODO se om denne er nødvendig
)
@DirtiesContext
@AutoConfigureRestTestClient()
class SecurityIntegrationTest {
    @Configuration
    @Import(SecurityConfig::class)
    @ComponentScan(
        basePackages = ["no.nav.familie.oppdrag"],
        excludeFilters = [
            ComponentScan.Filter(
                type = FilterType.REGEX, pattern = [".*[MQ].*"]
            ), ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = [SimuleringTjeneste::class]
            )],
    )
    class TestConfig {
        @Bean
        fun simuleringTjeneste(): SimuleringTjeneste =
            mockk<SimuleringTjeneste> {
                every { utførSimuleringOghentDetaljertSimuleringResultat(any()) } returns
                        DetaljertSimuleringResultat(
                            emptyList(),
                        )

                every { hentFeilutbetalinger(any()) } returns FeilutbetalingerFraSimulering(emptyList())
            }
    }

    @Autowired
    private lateinit var restTestClient: RestTestClient

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    private fun gyldigSimuleringRequest() =
        """
        {
          "kodeEndring": "NY",
          "fagSystem": "BA",
          "saksnummer": "TEST-123",
          "aktoer": "12345678901",
          "saksbehandlerId": "TEST-USER",
          "avstemmingTidspunkt": "2025-05-12T10:00:00",
          "utbetalingsperiode": []
        }
        """.trimIndent()

    @Nested
    inner class OffentligeEndepunkter {
        @Test
        fun `skal tillate tilgang til health uten token`() {
            val status = restTestClient
                .get()
                .uri("/internal/health")
                .exchange()
                .returnResult()
                .status

            assertThat(status).isNotIn(400..499)
        }

        @Test
        fun `skal tillate tilgang til actuator uten token`() {
            restTestClient
                .get()
                .uri("/internal/prometheus")
                .exchange()
                .expectStatus()
                .isOk
        }

        @Test
        fun `skal tillate tilgang til swagger-ui uten token`() {
            restTestClient
                .get()
                .uri("/swagger-ui.html")
                .exchange()
                .expectStatus()
                .isOk
        }

        @Test
        fun `skal tillate tilgang til api-docs uten token`() {
            restTestClient
                .get()
                .uri("/v3/api-docs")
                .exchange()
                .expectStatus()
                .isOk

        }

        @Nested
        inner class BeskyttedeEndepunkter {
            @Test
            fun `skal avvise simulering uten token`() {
                restTestClient
                    .post()
                    .uri("/api/simulering/v1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(gyldigSimuleringRequest())
                    .exchange()
                    .expectStatus()
                    .isUnauthorized
            }

            @Test
            fun `skal akseptere gyldig Azure AD-token`() {
                val token = JwtTokenTestUtil.lagAzureAdToken(mockOAuth2Server)

                val result =
                    restTestClient
                        .post()
                        .uri("/api/simulering/v1")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body((gyldigSimuleringRequest()))
                        .exchange()
                        .returnResult()

                val status = result.status
                assertNotEquals(
                    '4',
                    status.toString().first(),
                    message =
                        "Gyldig token burde ikke gi 4xx-feil, fikk $status",
                )
            }

            @Test
            fun `skal avvise utgått token`() {
                val token = JwtTokenTestUtil.lagUtgaattToken(mockOAuth2Server)
                restTestClient
                    .post()
                    .uri("/api/simulering/v1")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(gyldigSimuleringRequest())
                    .exchange()
                    .expectStatus()
                    .is4xxClientError
            }

            @Test
            fun `skal avvise token med feil issuer`() {
                val token = JwtTokenTestUtil.lagTokenMedFeilIssuer(mockOAuth2Server)
                val body = restTestClient
                    .post()
                    .uri("/api/simulering/v1")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(gyldigSimuleringRequest())
                    .exchange()
                    .expectStatus()
                    .isUnauthorized
                    .expectBody(Ressurs::class.java)
                    .returnResult()
                    .responseBody

                assertThat(body?.status).isEqualTo(Ressurs.Status.FEILET)
            }

            @Test
            fun `skal avvise token med feil audience`() {
                val token = JwtTokenTestUtil.lagTokenMedFeilAudience(mockOAuth2Server)

                val body = restTestClient
                    .post()
                    .uri("/api/simulering/v1")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(gyldigSimuleringRequest())
                    .exchange()
                    .expectStatus()
                    .isUnauthorized
                    .expectBody(Ressurs::class.java)
                    .returnResult()
                    .responseBody

                assertThat(body?.status).isEqualTo(Ressurs.Status.FEILET)
            }
        }
    }
}
