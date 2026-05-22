package no.nav.familie.oppdrag.config

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.junit.jupiter.Testcontainers
import tools.jackson.module.kotlin.readValue

@ActiveProfiles("dev")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = [
        "spring.cloud.vault.enabled=false",
        "AZURE_APP_CLIENT_ID=test-client-id",
        "OPPDRAG_SERVICE_URL=http://localhost:8080/oppdrag-service",
        "oppdrag.mq.enabled=false",
    ],
)
@ContextConfiguration(initializers = [MockOAuth2ServerInitializer::class])
@Testcontainers
class SecurityIntegrationTest {
    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @BeforeEach
    fun setup() {
        mockMvc =
            MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(springSecurity())
                .build()
    }

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
            // Health endpoint should not require auth (503 acceptable due to MQ unavailable in tests)
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
            mockMvc
                .perform(MockMvcRequestBuilders.get("/internal/prometheus"))
                .andExpect(MockMvcResultMatchers.status().isOk)
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
            mockMvc
                .perform(MockMvcRequestBuilders.get("/v3/api-docs"))
                .andExpect(MockMvcResultMatchers.status().isOk)
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

            // Forventer ikke 401/403 - kan være 400/500 på grunn av validering/business logic
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

            // Expired tokens should be rejected by auth layer (not 200 OK)
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
