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

}