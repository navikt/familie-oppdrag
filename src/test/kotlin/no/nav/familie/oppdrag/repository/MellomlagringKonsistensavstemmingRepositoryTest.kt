package no.nav.familie.oppdrag.repository

import no.nav.familie.oppdrag.service.Fagsystem
import no.nav.familie.oppdrag.util.Containers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID
import kotlin.test.assertEquals

@ActiveProfiles("dev")
@ContextConfiguration(initializers = arrayOf(Containers.PostgresSQLInitializer::class))
@SpringBootTest(
    classes = [MellomlagringKonsistensavstemmingRepositoryTest.TestConfig::class],
    properties = ["spring.cloud.vault.enabled=false"],
)
@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
@Testcontainers
internal class MellomlagringKonsistensavstemmingRepositoryTest {

    @Autowired lateinit var repository: MellomlagringKonsistensavstemmingRepository

    companion object {

        @Container var postgreSQLContainer = Containers.postgreSQLContainer
    }

    @Test
    fun `Test lesing aggregert beløp og antall oppgaver fra tom tabell`() {
        val transaksjonsId = UUID.randomUUID()
        assertEquals(0, repository.hentAggregertTotalBeløp(transaksjonsId))
        assertEquals(0, repository.hentAggregertAntallOppdrag(transaksjonsId))
    }

    @Test
    fun `Test lesing aggregert beløp og antall oppgaver`() {
        val transaksjonsId = UUID.randomUUID()

        repository.insert(opprettMellomlagringKonsistensavstemming(10, 161, transaksjonsId))
        repository.insert(opprettMellomlagringKonsistensavstemming(30, 222, transaksjonsId))

        assertEquals(383, repository.hentAggregertTotalBeløp(transaksjonsId))
        assertEquals(40, repository.hentAggregertAntallOppdrag(transaksjonsId))
    }

    @Test
    fun `Test nullstilling`() {
        val transaksjonsId = UUID.randomUUID()

        repository.insert(opprettMellomlagringKonsistensavstemming(10, 161, transaksjonsId))
        repository.insert(opprettMellomlagringKonsistensavstemming(30, 222, transaksjonsId))

        val transaksjonsId2 = UUID.randomUUID()
        assertEquals(0, repository.hentAggregertTotalBeløp(transaksjonsId2))
        assertEquals(0, repository.hentAggregertAntallOppdrag(transaksjonsId2))
    }

    fun opprettMellomlagringKonsistensavstemming(antallOppdrag: Int, totalBeløp: Long, transaksjonsId: UUID) =
        MellomlagringKonsistensavstemming(
            fagsystem = Fagsystem.BA,
            transaksjonsId = transaksjonsId,
            antallOppdrag = antallOppdrag,
            totalBeløp = totalBeløp,
        )

    @Configuration
    @ComponentScan(
        basePackages = ["no.nav.familie.oppdrag"],
        excludeFilters = [ComponentScan.Filter(type = FilterType.REGEX, pattern = [".*[MQ].*"])],
    )
    class TestConfig
}
