package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.oppdrag.Opphør
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.service.OppdragService
import org.junit.Assume
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.jms.annotation.EnableJms
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.DisabledIf
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Configuration
@ComponentScan("no.nav.familie.oppdrag") class TestConfig

@ActiveProfiles("dev")
@SpringBootTest(classes = [TestConfig::class], properties = ["spring.cloud.vault.enabled=false"])
@EnableJms
internal class OppdragControllerIntegrasjonTest {

    val localDateTimeNow = LocalDateTime.now()
    val localDateNow = LocalDate.now()

    val utbetalingsoppdragMedTilfeldigAktoer = Utbetalingsoppdrag(
            Utbetalingsoppdrag.KodeEndring.NY,
            "TEST",
            "SAKSNR",
            UUID.randomUUID().toString(), // Foreløpig plass til en 50-tegn string og ingen gyldighetssjekk
            "SAKSBEHANDLERID",
            localDateTimeNow,
            listOf(Utbetalingsperiode(false,
                                      Opphør(localDateNow),
                                      localDateNow,
                                      "KLASSE A",
                                      localDateNow,
                                      localDateNow,
                                      BigDecimal.ONE,
                                      Utbetalingsperiode.SatsType.MND,
                                      "UTEBETALES_TIL",
                                      1))
    )

    @Autowired lateinit var oppdragService: OppdragService

    @BeforeEach
    fun before() {
        assumeFalse("true".equals(System.getenv("CIRCLECI")));
        assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")));
    }

    @Test
    fun test_skal_lagre_oppdragprotokoll_for_utbetalingoppdrag() {

        val mapper = OppdragMapper()

        val oppdragController = OppdragController(oppdragService, mapper)

        oppdragController.sendOppdrag(utbetalingsoppdragMedTilfeldigAktoer)
    }

    @Test
    fun test_skal_ikke_lagre_duplikat() {

        val mapper = OppdragMapper()

        val oppdragController = OppdragController(oppdragService, mapper)

        val utbetalingsoppdrag = utbetalingsoppdragMedTilfeldigAktoer
        oppdragController.sendOppdrag(utbetalingsoppdrag)

        assertFailsWith<DuplicateKeyException> {
            oppdragController.sendOppdrag(utbetalingsoppdragMedTilfeldigAktoer.copy(aktoer = utbetalingsoppdrag.aktoer))
        }

    }

}