package no.nav.familie.oppdrag.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.oppdragId
import no.nav.familie.oppdrag.featuretoggle.FeatureToggleService
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.service.OppdragService
import no.nav.familie.oppdrag.util.Containers
import no.nav.familie.oppdrag.util.TestConfig
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.jms.annotation.EnableJms
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import java.time.Duration
import kotlin.test.assertEquals

@ActiveProfiles("dev")
@ContextConfiguration(initializers = [Containers.MQInitializer::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(classes = [TestConfig::class], properties = ["spring.cloud.vault.enabled=false"])
@EnableJms
@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
@Testcontainers
internal class OppdragControllerIntegrationTest(
    @Autowired private val oppdragService: OppdragService,
    @Autowired private val oppdragMapper: OppdragMapper,
    @Autowired private val oppdragLagerRepository: OppdragLagerRepository,
) {
    private val featureToggleService = mockk<FeatureToggleService>()
    private val oppdragController = OppdragController(oppdragService, oppdragMapper, featureToggleService)

    companion object {
        @Container
        private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:latest")

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword)
        }

        @Container var ibmMQContainer = Containers.ibmMQContainer
    }

    @Test
    fun `sendOppdrag skal lagre oppdrag for utbetalingoppdrag`() {
        every { featureToggleService.isEnabled(any(), true) } returns false

        val utbetalingsoppdrag = utbetalingsoppdragMedTilfeldigAktoer()
        oppdragController.sendOppdrag(utbetalingsoppdrag)

        assertOppdragStatus(utbetalingsoppdrag.oppdragId, OppdragStatus.KVITTERT_OK)
    }

    @Test
    fun `sendOppdrag skal returnere https statuscode 409 ved dobbel sending`() {
        every { featureToggleService.isEnabled(any(), true) } returns false

        val utbetalingsoppdrag = utbetalingsoppdragMedTilfeldigAktoer()

        val responseFørsteSending = oppdragController.sendOppdrag(utbetalingsoppdrag)
        assertEquals(HttpStatus.OK, responseFørsteSending.statusCode)
        assertEquals(Ressurs.Status.SUKSESS, responseFørsteSending.body?.status)

        val responseAndreSending = oppdragController.sendOppdrag(utbetalingsoppdrag)

        assertEquals(HttpStatus.CONFLICT, responseAndreSending.statusCode)
        assertEquals(Ressurs.Status.FEILET, responseAndreSending.body?.status)

        assertOppdragStatus(utbetalingsoppdrag.oppdragId, OppdragStatus.KVITTERT_OK)
    }

    @Test
    fun `sendOppdrag skal returnere 500 dersom toggelen SKRU_AV_IVERKSETTELSE er skrudd på`() {
        every { featureToggleService.isEnabled(any(), true) } returns true

        val utbetalingsoppdrag = utbetalingsoppdragMedTilfeldigAktoer()
        val response = oppdragController.sendOppdrag(utbetalingsoppdrag)

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(
            response.body?.melding,
        ).isEqualTo(
            "Iverksettelse er skrudd av for familie-oppdrag. All fremtidig iverksettelse skal gjøres via familie-oppdrag-backend i GCP (http://familie-oppdrag-backend).",
        )
    }

    @Test
    fun `resendOppdrag skal kunne resende et oppdrag hvis statusen er funksjonell feil`() {
        every { featureToggleService.isEnabled(any(), true) } returns false

        val utbetalingsoppdrag = utbetalingsoppdragMedTilfeldigAktoer()
        oppdragController.sendOppdrag(utbetalingsoppdrag)
        oppdragLagerRepository.oppdaterStatus(utbetalingsoppdrag.oppdragId, OppdragStatus.KVITTERT_FUNKSJONELL_FEIL)

        oppdragController.resendOppdrag(utbetalingsoppdrag.oppdragId)
        assertOppdragStatus(utbetalingsoppdrag.oppdragId, OppdragStatus.KVITTERT_OK)
    }

    @Test
    fun `resendOppdrag skal returnere 500 dersom toggelen SKRU_AV_IVERKSETTELSE er skrudd på`() {
        every { featureToggleService.isEnabled(any(), true) } returns false
        val utbetalingsoppdrag = utbetalingsoppdragMedTilfeldigAktoer()
        oppdragController.sendOppdrag(utbetalingsoppdrag)
        oppdragLagerRepository.oppdaterStatus(utbetalingsoppdrag.oppdragId, OppdragStatus.KVITTERT_FUNKSJONELL_FEIL)

        every { featureToggleService.isEnabled(any(), true) } returns true
        val response = oppdragController.resendOppdrag(utbetalingsoppdrag.oppdragId)
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(
            response.body?.melding,
        ).isEqualTo(
            "Iverksettelse er skrudd av for familie-oppdrag. All fremtidig iverksettelse skal gjøres via familie-oppdrag-backend i GCP (http://familie-oppdrag-backend).",
        )
    }

    private fun assertOppdragStatus(
        oppdragId: OppdragId,
        oppdragStatus: OppdragStatus,
    ) {
        await()
            .pollInterval(Duration.ofMillis(200))
            .atMost(Duration.ofSeconds(10))
            .untilAsserted {
                assertThat(oppdragLagerRepository.hentOppdrag(oppdragId).status).isEqualTo(oppdragStatus)
            }
    }
}
