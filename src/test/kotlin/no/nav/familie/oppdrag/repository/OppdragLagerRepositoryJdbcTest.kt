package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.oppdrag.iverksetting.Jaxb
import no.nav.familie.oppdrag.util.Containers
import no.nav.familie.oppdrag.util.TestConfig
import no.nav.familie.oppdrag.util.TestOppdragMedAvstemmingsdato
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer
import no.trygdeetaten.skjema.oppdrag.Mmel
import org.assertj.core.api.Assertions.assertThat
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
import java.util.UUID
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
        assertThat(kvitteringsmelding).isEqualToComparingFieldByField(hentetOppdatertOppdrag.kvitteringsmelding)
    }

    private fun kvitteringsmelding(): Mmel {
        val kvitteringsmelding = Jaxb.tilOppdrag(
            this::class.java.getResourceAsStream("/kvittering-avvist.xml")
                .bufferedReader().use { it.readText() },
        )
        return kvitteringsmelding.mmel
    }

    @Test
    fun skal_kun_hente_ut_ett_BA_oppdrag_for_grensesnittavstemming() {
        val dag = LocalDateTime.now()
        val startenPåDagen = dag.withHour(0).withMinute(0)
        val sluttenAvDagen = dag.withHour(23).withMinute(59)

        val avstemmingsTidspunktetSomSkalKjøres = dag

        val baOppdragLager = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(avstemmingsTidspunktetSomSkalKjøres, "BA").somOppdragLager
        val baOppdragLager2 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(dag.minusDays(1), "BA").somOppdragLager
        val efOppdragLager = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(dag, "EFOG").somOppdragLager

        oppdragLagerRepository.opprettOppdrag(baOppdragLager)
        oppdragLagerRepository.opprettOppdrag(baOppdragLager2)
        oppdragLagerRepository.opprettOppdrag(efOppdragLager)

        val oppdrageneTilGrensesnittavstemming = oppdragLagerRepository.hentIverksettingerForGrensesnittavstemming(startenPåDagen, sluttenAvDagen, "BA")

        assertEquals(1, oppdrageneTilGrensesnittavstemming.size)
        assertEquals("BA", oppdrageneTilGrensesnittavstemming.first().fagsystem)
        assertEquals(
            avstemmingsTidspunktetSomSkalKjøres.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss")),
            oppdrageneTilGrensesnittavstemming.first().avstemmingTidspunkt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss")),
        )
    }

    @Test
    fun skal_hente_ut_oppdrag_for_konsistensavstemming() {
        val forrigeMåned = LocalDateTime.now().minusMonths(1)
        val baOppdragLager = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(forrigeMåned, "BA").somOppdragLager
        val baOppdragLager2 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(forrigeMåned.minusDays(1), "BA").somOppdragLager
        oppdragLagerRepository.opprettOppdrag(baOppdragLager)
        oppdragLagerRepository.opprettOppdrag(baOppdragLager2)

        val utbetalingsoppdrag = oppdragLagerRepository.hentUtbetalingsoppdrag(baOppdragLager.id)
        val utbetalingsoppdrag2 = oppdragLagerRepository.hentUtbetalingsoppdrag(baOppdragLager2.id)

        assertEquals(baOppdragLager.utbetalingsoppdrag, utbetalingsoppdrag)
        assertEquals(baOppdragLager2.utbetalingsoppdrag, utbetalingsoppdrag2)
    }

    @Test
    fun `hentUtbetalingsoppdragForKonsistensavstemming går fint`() {
        val forrigeMåned = LocalDateTime.now().minusMonths(1)
        val utbetalingsoppdrag = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(forrigeMåned, "BA")
        val baOppdragLager = utbetalingsoppdrag.somOppdragLager.copy(status = OppdragStatus.KVITTERT_OK)
        oppdragLagerRepository.opprettOppdrag(baOppdragLager)
        oppdragLagerRepository.opprettOppdrag(baOppdragLager, 1)
        oppdragLagerRepository.opprettOppdrag(baOppdragLager, 2)
        val behandlingB = baOppdragLager.copy(behandlingId = UUID.randomUUID().toString())
        oppdragLagerRepository.opprettOppdrag(behandlingB)

        oppdragLagerRepository.opprettOppdrag(
            baOppdragLager.copy(
                fagsakId = UUID.randomUUID().toString(),
                behandlingId = UUID.randomUUID().toString(),
            ),
        )
        assertThat(
            oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(
                baOppdragLager.fagsystem,
                setOf("finnes ikke"),
            ),
        )
            .isEmpty()

        assertThat(
            oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(
                baOppdragLager.fagsystem,
                setOf(baOppdragLager.behandlingId),
            ),
        )
            .hasSize(1)

        assertThat(
            oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(
                baOppdragLager.fagsystem,
                setOf(
                    baOppdragLager.behandlingId,
                    behandlingB.behandlingId,
                ),
            ),
        )
            .hasSize(2)
    }

    @Test
    fun `hentUtbetalingsoppdragForKonsistensavstemming test at oppdeling av spørring går fint`() {
        val forrigeMåned = LocalDateTime.now().minusMonths(1)
        val utbetalingsoppdrag = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(forrigeMåned, "BA")
        val baOppdragLager = utbetalingsoppdrag.somOppdragLager.copy(status = OppdragStatus.KVITTERT_OK)

        oppdragLagerRepository.opprettOppdrag(baOppdragLager)
        val behandlingIder = mutableSetOf<String>()
        for (i in 1..5000) {
            val behandlingB = baOppdragLager.copy(behandlingId = baOppdragLager.behandlingId + i)
            behandlingIder.add(behandlingB.behandlingId)
            oppdragLagerRepository.opprettOppdrag(behandlingB)
        }

        assertThat(oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(baOppdragLager.fagsystem, behandlingIder))
            .hasSize(5000)
    }

    @Test
    fun `hentSisteUtbetalingsoppdragForFagsaker test spørring går fint`() {
        val forrigeMåned = LocalDateTime.now().minusMonths(1)
        val utbetalingsoppdrag1 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(forrigeMåned, "BA", fagsak = "1")
        val utbetalingsoppdrag2 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(forrigeMåned.minusDays(1), "BA", fagsak = "2")

        val oppdragLager1 = utbetalingsoppdrag1.somOppdragLager
        val oppdragLager2 = utbetalingsoppdrag2.somOppdragLager
        oppdragLagerRepository.opprettOppdrag(oppdragLager1)
        oppdragLagerRepository.opprettOppdrag(oppdragLager2)

        val hentedeOppdrag = oppdragLagerRepository.hentSisteUtbetalingsoppdragForFagsaker(
            fagsystem = oppdragLager1.fagsystem,
            fagsakIder = setOf(oppdragLager1.fagsakId, oppdragLager2.fagsakId),
        )

        assertThat(hentedeOppdrag.map { it.utbetalingsoppdrag }).containsAll(listOf(utbetalingsoppdrag1, utbetalingsoppdrag2))
    }
}
