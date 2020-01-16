package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.oppdrag.Opphør
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.dao.DuplicateKeyException
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertFailsWith

@Configuration
@ComponentScan("no.nav.familie.oppdrag") class TestConfig

@ActiveProfiles("dev")
@SpringBootTest(classes = [TestConfig::class], properties = ["spring.cloud.vault.enabled=false"])
@Disabled
internal class OppdragProtokollRepositoryJdbcTest {

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


    @Autowired lateinit var oppdragProtokollRepository: OppdragProtokollRepository

    @Test
    fun skal_ikke_lagre_duplikat() {

        oppdragProtokollRepository.opprettOppdrag(utbetalingsoppdragMedTilfeldigAktoer.somOppdragProtokoll)

        assertFailsWith<DuplicateKeyException> {
            oppdragProtokollRepository.opprettOppdrag(utbetalingsoppdragMedTilfeldigAktoer.somOppdragProtokoll)
        }

    }

    @Test
    fun skal_lagre_status() {

        val oppdragProtokoll = utbetalingsoppdragMedTilfeldigAktoer.somOppdragProtokoll
                .copy(status = OppdragProtokollStatus.LAGT_PÅ_KØ)

        oppdragProtokollRepository.opprettOppdrag(oppdragProtokoll)

        val hentetOppdragProtokoll = oppdragProtokollRepository.hentOppdrag(oppdragProtokoll.id)
        assertEquals(OppdragProtokollStatus.LAGT_PÅ_KØ, hentetOppdragProtokoll[0].status)

        oppdragProtokollRepository.oppdaterStatus(hentetOppdragProtokoll[0].id,OppdragProtokollStatus.KVITTERT_OK)

        val hentetOppdatertOppdragProtokoll = oppdragProtokollRepository.hentOppdrag(hentetOppdragProtokoll[0].id)
        assertEquals(OppdragProtokollStatus.KVITTERT_OK, hentetOppdatertOppdragProtokoll[0].status)

    }

}