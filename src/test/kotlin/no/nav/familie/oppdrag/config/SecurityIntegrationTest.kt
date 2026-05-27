package no.nav.familie.oppdrag.config

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import tools.jackson.module.kotlin.readValue

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = [SecurityIntegrationTest.TestConfig::class],
    properties = ["management.endpoint.health.validate-group-membership=false"],
)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@ContextConfiguration(initializers = [MockOAuth2ServerInitializer::class])
class SecurityIntegrationTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
    @Import(SecurityConfig::class)
    class TestConfig

    @Autowired
    private lateinit var mockMvc: MockMvc

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
            val result =
                mockMvc
                    .perform(MockMvcRequestBuilders.get("/internal/health"))
                    .andReturn()

            val status = result.response.status
            assert(status != HttpStatus.UNAUTHORIZED.value()) {
                "Health burde ikke gi 401, fikk $status"
            }
            assert(status != HttpStatus.FORBIDDEN.value()) {
                "Health burde ikke gi 403, fikk $status"
            }
        }

        @Test
        fun `skal tillate tilgang til actuator uten token`() {
            val result =
                mockMvc
                    .perform(MockMvcRequestBuilders.get("/internal/prometheus"))
                    .andReturn()

            val status = result.response.status
            assert(status != HttpStatus.UNAUTHORIZED.value()) {
                "Prometheus burde ikke gi 401, fikk $status"
            }
            assert(status != HttpStatus.FORBIDDEN.value()) {
                "Prometheus burde ikke gi 403, fikk $status"
            }
        }

        @Test
        fun `skal tillate tilgang til swagger-ui uten token`() {
            val result =
                mockMvc
                    .perform(MockMvcRequestBuilders.get("/swagger-ui.html"))
                    .andReturn()

            val status = result.response.status
            assert(status != HttpStatus.UNAUTHORIZED.value()) {
                "Swagger burde ikke gi 401, fikk $status"
            }
            assert(status != HttpStatus.FORBIDDEN.value()) {
                "Swagger burde ikke gi 403, fikk $status"
            }
        }

        @Test
        fun `skal tillate tilgang til api-docs uten token`() {
            val result =
                mockMvc
                    .perform(MockMvcRequestBuilders.get("/v3/api-docs"))
                    .andReturn()

            val status = result.response.status
            assert(status != HttpStatus.UNAUTHORIZED.value()) {
                "api-docs burde ikke gi 401, fikk $status"
            }
            assert(status != HttpStatus.FORBIDDEN.value()) {
                "api-docs burde ikke gi 403, fikk $status"
            }
        }
    }

    @Nested
    inner class BeskyttedeEndepunkter {
        @Test
        fun `skal avvise simulering uten token`() {
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/simulering/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gyldigSimuleringRequest()),
                ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
        }

        @Test
        fun `skal akseptere gyldig Azure AD-token`() {
            val token = JwtTokenTestUtil.lagAzureAdToken(mockOAuth2Server)

            val result =
                mockMvc
                    .perform(
                        MockMvcRequestBuilders
                            .post("/api/simulering/v1")
                            .header("Authorization", "Bearer $token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gyldigSimuleringRequest()),
                    ).andReturn()

            val status = result.response.status
            assert(status != HttpStatus.UNAUTHORIZED.value() && status != HttpStatus.FORBIDDEN.value()) {
                "Gyldig token burde ikke gi 401 eller 403, fikk $status"
            }
        }

        @Test
        fun `skal avvise utgått token`() {
            val token = JwtTokenTestUtil.lagUtgaattToken(mockOAuth2Server)

            val result =
                mockMvc
                    .perform(
                        MockMvcRequestBuilders
                            .post("/api/simulering/v1")
                            .header("Authorization", "Bearer $token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gyldigSimuleringRequest()),
                    ).andReturn()

            val status = result.response.status
            assert(status != HttpStatus.OK.value()) {
                "Expired token should be rejected, got status $status"
            }
        }

        @Test
        fun `skal avvise token med feil issuer`() {
            val token = JwtTokenTestUtil.lagTokenMedFeilIssuer(mockOAuth2Server)
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/simulering/v1")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gyldigSimuleringRequest()),
                ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect {
                    val content = jsonMapper.readValue<Ressurs<Any>>(it.response.contentAsString)
                    content.status == Ressurs.Status.FEILET
                }
        }

        @Test
        fun `skal avvise token med feil audience`() {
            val token = JwtTokenTestUtil.lagTokenMedFeilAudience(mockOAuth2Server)

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/simulering/v1")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gyldigSimuleringRequest()),
                ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect {
                    val content = jsonMapper.readValue<Ressurs<Any>>(it.response.contentAsString)
                    content.status == Ressurs.Status.FEILET
                }
        }
    }
}
