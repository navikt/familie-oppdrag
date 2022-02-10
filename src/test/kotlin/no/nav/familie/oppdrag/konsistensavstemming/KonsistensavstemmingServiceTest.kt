package no.nav.familie.oppdrag.konsistensavstemming

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingRequestV2
import no.nav.familie.kontrakter.felles.oppdrag.Opphør
import no.nav.familie.kontrakter.felles.oppdrag.PerioderForBehandling
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.repository.MellomlagringKonsistensavstemming
import no.nav.familie.oppdrag.repository.MellomlagringKonsistensavstemmingRepository
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.repository.UtbetalingsoppdragForKonsistensavstemming
import no.nav.familie.oppdrag.service.KonsistensavstemmingService
import no.nav.familie.oppdrag.service.MellomlagringKonsistensavstemmingService
import no.nav.virksomhet.tjenester.avstemming.informasjon.konsistensavstemmingsdata.v1.Konsistensavstemmingsdata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class KonsistensavstemmingServiceTest {

    private lateinit var konsistensavstemmingService: KonsistensavstemmingService
    private lateinit var oppdragLagerRepository: OppdragLagerRepository
    private lateinit var avstemmingSender: AvstemmingSender
    private lateinit var mellomlagringKonsistensavstemmingService: MellomlagringKonsistensavstemmingService
    private lateinit var mellomlagringKonsistensavstemmingRepository: MellomlagringKonsistensavstemmingRepository


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
        mellomlagringKonsistensavstemmingRepository = mockk()
        mellomlagringKonsistensavstemmingService = MellomlagringKonsistensavstemmingService(mellomlagringKonsistensavstemmingRepository)
        konsistensavstemmingService = KonsistensavstemmingService(avstemmingSender, oppdragLagerRepository, mellomlagringKonsistensavstemmingService)
        every { avstemmingSender.sendKonsistensAvstemming(any()) } just Runs

        every { mellomlagringKonsistensavstemmingRepository.hentAggregertAntallOppdrag(any()) } returns 0
        every { mellomlagringKonsistensavstemmingRepository.hentAggregertTotalBeløp(any()) } returns 0L

        val mkSlot: MellomlagringKonsistensavstemming = mockk()
        every { mellomlagringKonsistensavstemmingRepository.insert(any()) } returns mkSlot
    }

    @Test
    internal fun `plukker ut perioder fra 2 utbetalingsoppdrag fra samme fagsak til en melding`() {
        every { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(any(), eq(setOf("1", "2"))) } returns
                listOf(utbetalingsoppdrag1_1, utbetalingsoppdrag1_2)

        val perioder = listOf(PerioderForBehandling("1", setOf(1)),
                              PerioderForBehandling("2", setOf(3)))
        val request = KonsistensavstemmingRequestV2("BA", perioder, LocalDateTime.now())

        konsistensavstemmingService.utførKonsistensavstemming(request, true, true, null)

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

        konsistensavstemmingService.utførKonsistensavstemming(request, true, true, null)

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

    @Test
    internal fun `Sender startmelding uten oppdrag`() {
        every { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(any(), eq(emptySet())) } returns
                emptyList()
        every { mellomlagringKonsistensavstemmingRepository.insert(any()) } returns mockk()
        every { mellomlagringKonsistensavstemmingRepository.findAllByTransaksjonsId(any()) } returns emptyList()

        val avstemmingstidspunkt = LocalDateTime.now()
        val request = KonsistensavstemmingRequestV2("BA", emptyList(), avstemmingstidspunkt)
        val transaksjonsId = UUID.randomUUID().toString()

        konsistensavstemmingService.utførKonsistensavstemming(request, true, false, transaksjonsId)

        val transaksjonsIdSlot = slot<String>()
        val startmeldingSlot = slot<Konsistensavstemmingsdata>()
        val mkSlot = slot<MellomlagringKonsistensavstemming>()

        verify(exactly = 1) { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(any(), eq(emptySet())) }
        verify(exactly = 1) { mellomlagringKonsistensavstemmingRepository.findAllByTransaksjonsId(capture(transaksjonsIdSlot)) }
        verify(exactly = 1) { avstemmingSender.sendKonsistensAvstemming(capture(startmeldingSlot)) }
        verify(exactly = 1) { mellomlagringKonsistensavstemmingRepository.insert(capture(mkSlot)) }

        assertEquals(transaksjonsId, transaksjonsIdSlot.captured)
        assertEquals(0, mkSlot.captured.antallOppdrag)
        assertEquals(0L, mkSlot.captured.totalBeløp)
        assertEquals("START", startmeldingSlot.captured.aksjonsdata.aksjonsType)
    }

    @Test
    internal fun `Sender avsluttmelding uten oppdrag`() {
        every { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(any(), eq(emptySet())) } returns
                emptyList()

        val avstemmingstidspunkt = LocalDateTime.now()
        val request = KonsistensavstemmingRequestV2("BA", emptyList(), avstemmingstidspunkt)
        val transaksjonsId = UUID.randomUUID().toString()

        konsistensavstemmingService.utførKonsistensavstemming(request, false, true, transaksjonsId)

        val totalDataMeldingSlot = slot<Konsistensavstemmingsdata>()
        val avsluttmeldingSlot = slot<Konsistensavstemmingsdata>()

        verify(exactly = 1) { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(any(), eq(emptySet())) }

        verifyOrder {
            avstemmingSender.sendKonsistensAvstemming(capture(totalDataMeldingSlot))
            avstemmingSender.sendKonsistensAvstemming(capture(avsluttmeldingSlot))
        }

        verify(exactly = 0) { mellomlagringKonsistensavstemmingRepository.insert(any()) }
        verify(exactly = 0) { mellomlagringKonsistensavstemmingRepository.findAllByTransaksjonsId(any()) }

        assertEquals(BigInteger.valueOf(0), totalDataMeldingSlot.captured.totaldata.totalAntall)
        assertEquals(BigDecimal.valueOf(0), totalDataMeldingSlot.captured.totaldata.totalBelop)
        assertEquals("AVSL", avsluttmeldingSlot.captured.aksjonsdata.aksjonsType)
    }

    @Test
    internal fun `Sender oppdragsmeldinger uten start eller avslutt melding`() {
        every { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(any(), eq(setOf("1", "3"))) } returns
                listOf(utbetalingsoppdrag1_1, utbetalingsoppdrag2_1)
        every { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(any(), eq(emptySet())) } returns
                emptyList()
        every { mellomlagringKonsistensavstemmingRepository.insert(any()) } returns mockk()

        val avstemmingstidspunkt = LocalDateTime.now()
        val perioder = listOf(PerioderForBehandling("1", setOf(1)),
                              PerioderForBehandling("3", setOf(1, 2)))

        val request = KonsistensavstemmingRequestV2("BA", perioder, avstemmingstidspunkt)
        val transaksjonsId = UUID.randomUUID().toString()

        konsistensavstemmingService.utførKonsistensavstemming(request, false, false, transaksjonsId)

        val oppdrag1Slot = slot<Konsistensavstemmingsdata>()
        val oppdrag3Slot = slot<Konsistensavstemmingsdata>()
        val mkSlot = slot<MellomlagringKonsistensavstemming>()
        val totalBeløpSlot = slot<Long>()

        verify(exactly = 1) { oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming("BA", eq(setOf("1", "3"))) }
        verify(exactly = 0) { mellomlagringKonsistensavstemmingRepository.findAllByTransaksjonsId(any()) }

        verifyOrder {
            avstemmingSender.sendKonsistensAvstemming(capture(oppdrag1Slot))
            avstemmingSender.sendKonsistensAvstemming(capture(oppdrag3Slot))
        }

        verify(exactly = 1) { mellomlagringKonsistensavstemmingRepository.insert(capture(mkSlot)) }

        assertEquals(1, oppdrag1Slot.captured.oppdragsdataListe.size)
        assertEquals(1, oppdrag3Slot.captured.oppdragsdataListe.size)
        assertEquals(2, mkSlot.captured.antallOppdrag)
        assertEquals(161, mkSlot.captured.totalBeløp)
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