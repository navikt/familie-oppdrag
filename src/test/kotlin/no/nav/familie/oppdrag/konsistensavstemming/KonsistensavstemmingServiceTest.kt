package no.nav.familie.oppdrag.konsistensavstemming

import io.mockk.*
import no.nav.familie.kontrakter.felles.oppdrag.OppdragIdForFagsystem
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.repository.somOppdragLager
import no.nav.familie.oppdrag.repository.somOppdragLagerMedVersjon
import no.nav.familie.oppdrag.service.KonsistensavstemmingService
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class KonsistensavstemmingServiceTest {

    private lateinit var konsistensavstemmingService: KonsistensavstemmingService
    private lateinit var oppdragLagerRepository: OppdragLagerRepository
    private lateinit var avstemmingSender: AvstemmingSender

    @BeforeEach
    fun setUp() {
        oppdragLagerRepository = mockk()
        avstemmingSender = mockk()
        konsistensavstemmingService = KonsistensavstemmingService(avstemmingSender, oppdragLagerRepository)
    }

    @Test
    fun skal_konsistensavstemme_riktig_versjon() {
        val oppdrag = TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer().somOppdragLager
                .apply { status = OppdragStatus.KVITTERT_FUNKSJONELL_FEIL }
        val oppdragV1 = TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer().somOppdragLagerMedVersjon(1)
                .apply { status = OppdragStatus.KVITTERT_OK }

        every { oppdragLagerRepository.hentAlleVersjonerAvOppdrag(any()) } returns
                listOf(oppdrag, oppdragV1)
        every { oppdragLagerRepository.hentUtbetalingsoppdrag(any(), any()) } returns
                TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer()
        every { avstemmingSender.sendKonsistensAvstemming(any()) } just Runs

        konsistensavstemmingService.utførKonsistensavstemming(
                "BA",
                listOf(OppdragIdForFagsystem(oppdrag.personIdent, oppdrag.behandlingId.toLong())),
                LocalDateTime.now()
        )

        verify (exactly = 4) { avstemmingSender.sendKonsistensAvstemming(any()) }
        verify (exactly = 0) { oppdragLagerRepository.hentUtbetalingsoppdrag(any(), 0) }
        verify (exactly = 1) { oppdragLagerRepository.hentUtbetalingsoppdrag(any(), 1) }
    }
}