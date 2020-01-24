package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.oppdrag.iverksetting.Jaxb
import no.nav.familie.oppdrag.util.Containers
import no.nav.familie.oppdrag.util.TestConfig
import no.nav.familie.oppdrag.util.TestOppdragMedAvstemmingsdato
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer
import no.trygdeetaten.skjema.oppdrag.Mmel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertFailsWith

@ActiveProfiles("dev")
@ContextConfiguration(initializers = arrayOf(Containers.PostgresSQLInitializer::class))
@SpringBootTest(classes = [TestConfig::class], properties = ["spring.cloud.vault.enabled=false"])
@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
@Testcontainers
internal class OppdragLagerRepositoryJdbcTest {

    @Autowired lateinit var oppdragLagerRepository: OppdragLagerRepository

    companion object {
        @Container var postgreSQLContainer = Containers.postgreSQLContainer
    }

    @Test
    fun skal_ikke_lagre_duplikat() {

        val oppdragLager = utbetalingsoppdragMedTilfeldigAktoer().somOppdragLager

        oppdragLagerRepository.opprettOppdrag(oppdragLager)

        assertFailsWith<DuplicateKeyException> {
            oppdragLagerRepository.opprettOppdrag(oppdragLager)
        }
    }

    @Test
    fun skal_lagre_status() {

        val oppdragLager = utbetalingsoppdragMedTilfeldigAktoer().somOppdragLager
                .copy(status = OppdragStatus.LAGT_PÅ_KØ)

        oppdragLagerRepository.opprettOppdrag(oppdragLager)

        val hentetOppdrag = oppdragLagerRepository.hentOppdrag(oppdragLager.id)
        assertEquals(OppdragStatus.LAGT_PÅ_KØ, hentetOppdrag.status)

        oppdragLagerRepository.oppdaterStatus(hentetOppdrag.id, OppdragStatus.KVITTERT_OK)

        val hentetOppdatertOppdrag = oppdragLagerRepository.hentOppdrag(hentetOppdrag.id)
        assertEquals(OppdragStatus.KVITTERT_OK, hentetOppdatertOppdrag.status)

    }

    @Test
    fun skal_lagre_kvitteringsmelding() {
        val oppdragLager = utbetalingsoppdragMedTilfeldigAktoer().somOppdragLager
                .copy(status = OppdragStatus.LAGT_PÅ_KØ)

        oppdragLagerRepository.opprettOppdrag(oppdragLager)
        val hentetOppdrag = oppdragLagerRepository.hentOppdrag(oppdragLager.id)
        val kvitteringsmelding = kvitteringsmelding()

        oppdragLagerRepository.oppdaterKvitteringsmelding(hentetOppdrag.id, kvitteringsmelding)

        val hentetOppdatertOppdrag = oppdragLagerRepository.hentOppdrag(oppdragLager.id)
        assertEquals(objectMapper.writeValueAsString(kvitteringsmelding), hentetOppdatertOppdrag.kvitteringsmelding)
    }

    private fun kvitteringsmelding(): Mmel {
        val kvitteringsmelding = Jaxb().tilOppdrag(this::class.java.getResourceAsStream("/kvittering-avvist.xml")
                .bufferedReader().use { it.readText() })
        return kvitteringsmelding.mmel
    }

    @Test
    fun skal_kun_hente_ut_ett_BA_oppdrag_for_grensesnittavstemming() {
        val startenPåDagen = LocalDateTime.now().withHour(0).withMinute(0)
        val sluttenAvDagen = LocalDateTime.now().withHour(23).withMinute(59)

        val avstemmingsTidspunktetSomSkalKjøres = LocalDateTime.now()

        val baOppdragLager = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(avstemmingsTidspunktetSomSkalKjøres, "BA").somOppdragLager
        val baOppdragLager2 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(LocalDateTime.now().minusDays(1), "BA").somOppdragLager
        val efOppdragLager = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(LocalDateTime.now(), "EF").somOppdragLager

        oppdragLagerRepository.opprettOppdrag(baOppdragLager)
        oppdragLagerRepository.opprettOppdrag(baOppdragLager2)
        oppdragLagerRepository.opprettOppdrag(efOppdragLager)

        val oppdrageneTilGrensesnittavstemming = oppdragLagerRepository.hentIverksettingerForGrensesnittavstemming(startenPåDagen, sluttenAvDagen, "BA")

        assertEquals(1, oppdrageneTilGrensesnittavstemming.size)
        assertEquals("BA", oppdrageneTilGrensesnittavstemming.first().fagsystem)
        assertEquals(avstemmingsTidspunktetSomSkalKjøres.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSS")),
                oppdrageneTilGrensesnittavstemming.first().avstemmingTidspunkt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSS")))
    }
}