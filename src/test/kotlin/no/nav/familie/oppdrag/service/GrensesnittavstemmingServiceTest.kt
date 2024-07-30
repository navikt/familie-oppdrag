package no.nav.familie.oppdrag.service

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.kontrakter.felles.oppdrag.GrensesnittavstemmingRequest
import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.repository.TidligereKjørtGrensesnittavstemming
import no.nav.familie.oppdrag.repository.TidligereKjørteGrensesnittavstemmingerRepository
import no.nav.familie.oppdrag.repository.somAvstemming
import no.nav.familie.oppdrag.util.TestOppdragMedAvstemmingsdato
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.AksjonType
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Avstemmingsdata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional

class GrensesnittavstemmingServiceTest {
    val fagområde = "EFOG"
    val antall = 2

    val avstemmingSender = mockk<AvstemmingSender>()
    val oppdragLagerRepository = mockk<OppdragLagerRepository>()
    val tidligereKjørteGrensesnittavstemmingerRepository = mockk<TidligereKjørteGrensesnittavstemmingerRepository>()
    val grensesnittavstemmingService =
        GrensesnittavstemmingService(
            avstemmingSender = avstemmingSender,
            oppdragLagerRepository = oppdragLagerRepository,
            tidligereKjørteGrensesnittavstemmingerRepository = tidligereKjørteGrensesnittavstemmingerRepository,
            antall = antall,
        )

    val slot = mutableListOf<Avstemmingsdata>()

    @BeforeEach
    fun setUp() {
        slot.clear()
        every {
            oppdragLagerRepository.hentIverksettingerForGrensesnittavstemming(any(), any(), any(), antall, any())
        } returns emptyList()

        every {
            tidligereKjørteGrensesnittavstemmingerRepository.findById(
                any(),
            )
        } returns Optional.empty<TidligereKjørtGrensesnittavstemming>()

        justRun { avstemmingSender.sendGrensesnittAvstemming(capture(slot)) }
    }

    @Test
    fun `skal sende en melding på mq per batch`() {
        every {
            oppdragLagerRepository.hentIverksettingerForGrensesnittavstemming(
                any(),
                any(),
                any(),
                antall,
                0,
            )
        } returns
            listOf(
                TestOppdragMedAvstemmingsdato
                    .lagTestUtbetalingsoppdrag(
                        LocalDateTime.now(),
                        fagområde,
                    ).somAvstemming,
                TestOppdragMedAvstemmingsdato
                    .lagTestUtbetalingsoppdrag(
                        LocalDateTime.now(),
                        fagområde,
                    ).somAvstemming,
            )
        every {
            oppdragLagerRepository.hentIverksettingerForGrensesnittavstemming(
                any(),
                any(),
                any(),
                antall,
                1,
            )
        } returns
            listOf(
                TestOppdragMedAvstemmingsdato
                    .lagTestUtbetalingsoppdrag(
                        LocalDateTime.now(),
                        fagområde,
                    ).somAvstemming,
            )

        grensesnittavstemmingService.utførGrensesnittavstemming(
            GrensesnittavstemmingRequest(
                fagsystem = fagområde,
                fra = LocalDateTime.now(),
                til = LocalDateTime.now(),
                avstemmingId = null,
            ),
        )

        verify(exactly = 3) {
            oppdragLagerRepository.hentIverksettingerForGrensesnittavstemming(any(), any(), any(), antall, any())
        }
        assertThat(slot).hasSize(5)
        assertThat(slot[0].aksjon.aksjonType).isEqualTo(AksjonType.START)
        assertThat(slot[1].aksjon.aksjonType).isEqualTo(AksjonType.DATA)
        assertThat(slot[2].aksjon.aksjonType).isEqualTo(AksjonType.DATA)
        assertThat(slot[3].aksjon.aksjonType).isEqualTo(AksjonType.DATA)
        assertThat(slot[4].aksjon.aksjonType).isEqualTo(AksjonType.AVSL)

        // Kun datameldinger skal ha detaljer
        assertThat(slot[1].detalj).hasSize(2)
        assertThat(slot[2].detalj).hasSize(1)
        assertThat(slot[3].detalj).isEmpty() // totaldata

        assertThat(slot[3].total.totalAntall).isEqualTo(3)
    }
}
