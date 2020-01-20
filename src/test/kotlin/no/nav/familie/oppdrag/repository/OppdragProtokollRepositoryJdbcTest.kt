package no.nav.familie.oppdrag.repository


import no.nav.familie.oppdrag.util.Containers
import no.nav.familie.oppdrag.util.TestConfig
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer
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
import kotlin.test.assertFailsWith

@ActiveProfiles("dev")
@ContextConfiguration(initializers = arrayOf(Containers.PostgresSQLInitializer::class))
@SpringBootTest(classes = [TestConfig::class], properties = ["spring.cloud.vault.enabled=false"])
@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
@Testcontainers
internal class OppdragProtokollRepositoryJdbcTest {

    @Autowired lateinit var oppdragProtokollRepository: OppdragProtokollRepository

    companion object {
        @Container var postgreSQLContainer = Containers.postgreSQLContainer
    }

    @Test
    fun skal_ikke_lagre_duplikat() {

        val oppdragProtokoll = utbetalingsoppdragMedTilfeldigAktoer().somOppdragProtokoll

        oppdragProtokollRepository.opprettOppdrag(oppdragProtokoll)

        assertFailsWith<DuplicateKeyException> {
            oppdragProtokollRepository.opprettOppdrag(oppdragProtokoll)
        }
    }

    @Test
    fun skal_lagre_status() {

        val oppdragProtokoll = utbetalingsoppdragMedTilfeldigAktoer().somOppdragProtokoll
                .copy(status = OppdragProtokollStatus.LAGT_PÅ_KØ)

        oppdragProtokollRepository.opprettOppdrag(oppdragProtokoll)

        val hentetOppdragProtokoll = oppdragProtokollRepository.hentOppdrag(oppdragProtokoll.id)
        assertEquals(OppdragProtokollStatus.LAGT_PÅ_KØ, hentetOppdragProtokoll.status)

        oppdragProtokollRepository.oppdaterStatus(hentetOppdragProtokoll.id, OppdragProtokollStatus.KVITTERT_OK)

        val hentetOppdatertOppdragProtokoll = oppdragProtokollRepository.hentOppdrag(hentetOppdragProtokoll.id)
        assertEquals(OppdragProtokollStatus.KVITTERT_OK, hentetOppdatertOppdragProtokoll.status)

    }

}