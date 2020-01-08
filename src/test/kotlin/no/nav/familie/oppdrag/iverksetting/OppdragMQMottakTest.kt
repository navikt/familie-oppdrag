package no.nav.familie.oppdrag.iverksetting

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.env.Environment
import java.io.File
import kotlin.test.assertEquals


class OppdragMQMottakTest {

    lateinit var oppdragMottaker: OppdragMottaker

    @BeforeEach
    fun setUp() {
        val env = mockk<Environment>()
        val oppdragProtokollRepository  = mockk<OppdragProtokollRepository>()
        every { env.activeProfiles } returns emptyArray()

        oppdragMottaker = OppdragMottaker(oppdragProtokollRepository, env)
    }

    @Test
    fun skal_tolke_kvittering_riktig_ved_OK() {
        val kvittering: String = lesKvittering("kvittering-akseptert.xml")
        val statusFraKvittering = oppdragMottaker.lesKvittering(kvittering)
        assertEquals(Status.OK, statusFraKvittering)
    }

    @Test
    fun skal_tolke_kvittering_riktig_ved_feil() {
        val kvittering: String = lesKvittering("kvittering-avvist.xml")
        val statusFraKvittering = oppdragMottaker.lesKvittering(kvittering)
        assertEquals(Status.AVVIST_FUNKSJONELLE_FEIL, statusFraKvittering)
    }

    private fun lesKvittering(filnavn: String): String {
        return File("src/test/resources/$filnavn").inputStream().readBytes().toString(Charsets.UTF_8)
    }
}