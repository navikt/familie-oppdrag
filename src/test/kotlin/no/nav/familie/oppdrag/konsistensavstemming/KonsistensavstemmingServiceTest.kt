package no.nav.familie.oppdrag.konsistensavstemming

import io.mockk.*
import no.nav.familie.kontrakter.felles.oppdrag.*
import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.repository.OppdragRepository
import no.nav.familie.oppdrag.repository.UtbetalingsoppdragForKonsistensavstemming
import no.nav.familie.oppdrag.repository.somOppdragLager
import no.nav.familie.oppdrag.repository.somOppdragLagerMedVersjon
import no.nav.familie.oppdrag.service.KonsistensavstemmingService
import no.nav.familie.oppdrag.util.TestUtbetalingsoppdrag
import no.nav.virksomhet.tjenester.avstemming.informasjon.konsistensavstemmingsdata.v1.Konsistensavstemmingsdata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class KonsistensavstemmingServiceTest {

    private lateinit var konsistensavstemmingService: KonsistensavstemmingService
    private lateinit var oppdragLagerRepository: OppdragRepository
    private lateinit var avstemmingSender: AvstemmingSender

    private val saksnummer = "1"
    private val saksnummer2 = "2"

    private val utbetalingsoppdrag1_1 =
            lagUtbetalingsoppdrag(saksnummer,
                                  "1",
                                  lagUtbetalingsperiode(periodeId = 1, beløp = 111, behandlingsId = 1),
                                  lagUtbetalingsperiode(periodeId = 2, beløp = 100, behandlingsId = 1))

    // Opphør på periode 2, ny periode med annet beløp
    private val utbetalingsoppdrag1_2 =
            lagUtbetalingsoppdrag(saksnummer,
                                  "2",
                                  lagUtbetalingsperiode(periodeId = 2, beløp = 100, behandlingsId = 1, opphør = true),
                                  lagUtbetalingsperiode(periodeId = 3, beløp = 211, behandlingsId = 2))
    private val utbetalingsoppdrag2_1 =
            lagUtbetalingsoppdrag(saksnummer2,
                                  "3",
                                  lagUtbetalingsperiode(periodeId = 1, beløp = 20, behandlingsId = 3),
                                  lagUtbetalingsperiode(periodeId = 2, beløp = 30, behandlingsId = 3))

    @BeforeEach
    fun setUp() {
        oppdragLagerRepository = mockk()
        avstemmingSender = mockk()
        konsistensavstemmingService = KonsistensavstemmingService(avstemmingSender, oppdragLagerRepository)
        every { avstemmingSender.sendKonsistensAvstemming(any()) } just Runs
    }

    @Test
    fun skal_konsistensavstemme_riktig_versjon() {
        val oppdrag = TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer().somOppdragLager
                .apply { status = OppdragStatus.KVITTERT_FUNKSJONELL_FEIL }
        val oppdragV1 = TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer().somOppdragLagerMedVersjon(1)
                .apply { status = OppdragStatus.KVITTERT_OK }

        every { oppdragLagerRepository.hentAlleVersjonerAvOppdrag(any(), any(),any()) } returns
                listOf(oppdrag, oppdragV1)
        every { oppdragLagerRepository.hentUtbetalingsoppdrag(any(), any(),any(),any()) } returns
                TestUtbetalingsoppdrag.utbetalingsoppdragMedTilfeldigAktoer()

        konsistensavstemmingService.utførKonsistensavstemming(KonsistensavstemmingRequest(
                "BA",
                listOf(OppdragIdForFagsystem(oppdrag.personIdent, oppdrag.behandlingId.toLong())),
                LocalDateTime.now()
        ))

        verify(exactly = 4) { avstemmingSender.sendKonsistensAvstemming(any()) }
        verify(exactly = 0) { oppdragLagerRepository.hentUtbetalingsoppdrag(any(),any(),any(), 0) }
        verify(exactly = 1) { oppdragLagerRepository.hentUtbetalingsoppdrag(any(),any(),any(), 1) }
    }

    @Test
    internal fun `plukker ut perioder fra 2 utbetalingsoppdrag fra samme fagsak til en melding`() {
        every { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(any(), eq(setOf("1", "2"))) } returns
                listOf(utbetalingsoppdrag1_1, utbetalingsoppdrag1_2)

        val perioder = listOf(PerioderForBehandling("1", setOf(1)),
                              PerioderForBehandling("2", setOf(3)))
        val request = KonsistensavstemmingRequestV2("BA", perioder, LocalDateTime.now())

        konsistensavstemmingService.utførKonsistensavstemming(request)

        val oppdrag = slot<Konsistensavstemmingsdata>()
        val totalData = slot<Konsistensavstemmingsdata>()
        verifyOrder {
            avstemmingSender.sendKonsistensAvstemming(any())
            avstemmingSender.sendKonsistensAvstemming(capture(oppdrag))
            avstemmingSender.sendKonsistensAvstemming(capture(totalData))
            avstemmingSender.sendKonsistensAvstemming(any())
        }

        assertThat(oppdrag.captured.oppdragsdataListe).hasSize(1)
        assertThat(oppdrag.captured.oppdragsdataListe[0].oppdragslinjeListe).hasSize(2)

        assertThat(totalData.captured.totaldata.totalBelop.toInt()).isEqualTo(322)
        assertThat(totalData.captured.totaldata.totalAntall.toInt()).isEqualTo(1)
    }

    @Test
    internal fun `sender hver fagsak i ulike meldinger`() {
        every { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(any(), eq(setOf("1", "3"))) } returns
                listOf(utbetalingsoppdrag1_1, utbetalingsoppdrag2_1)

        val perioder = listOf(PerioderForBehandling("1", setOf(1)),
                              PerioderForBehandling("3", setOf(1, 2)))

        val request = KonsistensavstemmingRequestV2("BA", perioder, LocalDateTime.now())

        konsistensavstemmingService.utførKonsistensavstemming(request)

        val oppdrag = slot<Konsistensavstemmingsdata>()
        val oppdrag2 = slot<Konsistensavstemmingsdata>()
        val totalData = slot<Konsistensavstemmingsdata>()
        verifyOrder {
            avstemmingSender.sendKonsistensAvstemming(any())
            avstemmingSender.sendKonsistensAvstemming(capture(oppdrag))
            avstemmingSender.sendKonsistensAvstemming(capture(oppdrag2))
            avstemmingSender.sendKonsistensAvstemming(capture(totalData))
            avstemmingSender.sendKonsistensAvstemming(any())
        }

        assertThat(oppdrag.captured.oppdragsdataListe).hasSize(1)
        assertThat(oppdrag.captured.oppdragsdataListe[0].oppdragslinjeListe).hasSize(1)

        assertThat(oppdrag2.captured.oppdragsdataListe).hasSize(1)
        assertThat(oppdrag2.captured.oppdragsdataListe[0].oppdragslinjeListe).hasSize(2)

        assertThat(totalData.captured.totaldata.totalBelop.toInt()).isEqualTo(161)
        assertThat(totalData.captured.totaldata.totalAntall.toInt()).isEqualTo(2)
    }

    private fun lagUtbetalingsperiode(
            periodeId: Long,
            forrigePeriodeId: Long? = null,
            beløp: Int,
            behandlingsId: Long,
            opphør: Boolean = false,
    ) =
            Utbetalingsperiode(erEndringPåEksisterendePeriode = false,
                               opphør = if (opphør) Opphør(LocalDate.now()) else null,
                               periodeId = periodeId,
                               forrigePeriodeId = forrigePeriodeId,
                               datoForVedtak = LocalDate.now(),
                               klassifisering = "EF",
                               vedtakdatoFom = LocalDate.now().minusYears(1),
                               vedtakdatoTom = LocalDate.now().plusYears(1),
                               sats = BigDecimal(beløp),
                               satsType = Utbetalingsperiode.SatsType.MND,
                               utbetalesTil = "meg",
                               behandlingId = behandlingsId)

    private fun lagUtbetalingsoppdrag(saksnummer: String, behandlingId: String, vararg utbetalingsperiode: Utbetalingsperiode) =
            UtbetalingsoppdragForKonsistensavstemming(saksnummer,
                                                      behandlingId,
                                                      Utbetalingsoppdrag(kodeEndring = Utbetalingsoppdrag.KodeEndring.NY,
                                                                         fagSystem = "BA",
                                                                         saksnummer = saksnummer,
                                                                         aktoer = "aktoer",
                                                                         saksbehandlerId = "saksbehandler",
                                                                         utbetalingsperiode = utbetalingsperiode.toList()))
}