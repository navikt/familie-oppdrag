package no.nav.familie.oppdrag.simulering

import no.nav.familie.oppdrag.simulering.mock.SimuleringGenerator
import no.nav.system.os.entiteter.typer.simpletypes.KodeStatusLinje
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdrag
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdragslinje
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SplitResponsePåFagsakIdTest {
    private val simuleringGenerator = SimuleringGenerator()

    @Test
    fun `splitResponsPåFagsakId splitter respons på fagsakId`() {
        // Arrange
        val fagsystemId = "987654321"
        val oppdragGjelderId = "12345678901"
        val kodeEndring = "NY"
        val request: SimulerBeregningRequest = opprettSimulerBeregningRequest(oppdragGjelderId, kodeEndring)
        request.request.oppdrag.oppdragslinje.add(
            opprettOppdragslinje("NY", null, 2339, oppdragGjelderId, "2020-06-01", "2020-11-30", null),
        )

        val response = simuleringGenerator.opprettSimuleringsResultat(request)

        response.response.simulering.beregningsPeriode
            .first()
            .beregningStoppnivaa
            .first()
            .fagsystemId = fagsystemId

        // Act
        val fagsystemIdRespons = getFagsystemIdsFromResponse(response)

        val (responsForFagsak, beregningsPerioderForAndreFagsaker) =
            splitResponsePåFagsakId(
                response = response,
                fagsakId = fagsystemId,
            )

        val fagsystemIdResponsForFagsak = getFagsystemIdsFromResponse(responsForFagsak)
        byttUtBeregningStoppnivaa(response, beregningsPerioderForAndreFagsaker, fagsystemId)

        val fagsystemIdResponsForAndreFagsaker = getFagsystemIdsFromResponse(response)

        // Assert
        assertThat(fagsystemIdResponsForAndreFagsaker.size + fagsystemIdResponsForFagsak.size).isEqualTo(
            fagsystemIdRespons.size,
        )
        assertThat(fagsystemIdResponsForFagsak.size).isEqualTo(1)
        assertThat(fagsystemIdResponsForFagsak.single()).isEqualTo(fagsystemId)
    }

    private fun getFagsystemIdsFromResponse(respons: SimulerBeregningResponse): List<String> =
        respons.response.simulering.beregningsPeriode.flatMap { beregningsPeriode ->
            beregningsPeriode.beregningStoppnivaa.map { stoppnivaa -> stoppnivaa.fagsystemId }
        }

    private fun opprettSimulerBeregningRequest(
        oppdragGjelderId: String,
        kodeEndring: String,
    ): SimulerBeregningRequest {
        val request = SimulerBeregningRequest()
        request.request =
            no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes
                .SimulerBeregningRequest()
        request.request.oppdrag = Oppdrag()
        request.request.oppdrag.kodeEndring = kodeEndring
        request.request.oppdrag.kodeFagomraade = "BA"
        request.request.oppdrag.fagsystemId = "323456789"
        request.request.oppdrag.oppdragGjelderId = oppdragGjelderId
        request.request.oppdrag.saksbehId = "saksbeh"
        return request
    }

    private fun opprettOppdragslinje(
        kodeEndringLinje: String,
        kodeStatusLinje: KodeStatusLinje?,
        sats: Long,
        utbetalesTilId: String,
        datoVedtakFom: String,
        datoVedtakTom: String,
        datoStatusFom: String?,
    ): Oppdragslinje {
        val oppdragslinje = Oppdragslinje()
        oppdragslinje.kodeEndringLinje = kodeEndringLinje
        oppdragslinje.kodeStatusLinje = kodeStatusLinje
        oppdragslinje.vedtakId = "2020-11-27"
        oppdragslinje.delytelseId = "1122334455667700"
        oppdragslinje.kodeKlassifik = "FPADATORD"
        oppdragslinje.datoVedtakFom = datoVedtakFom
        oppdragslinje.datoVedtakTom = datoVedtakTom
        oppdragslinje.datoStatusFom = datoStatusFom
        oppdragslinje.sats = BigDecimal.valueOf(sats)
        oppdragslinje.typeSats = "MND"
        oppdragslinje.saksbehId = "saksbeh"
        oppdragslinje.utbetalesTilId = utbetalesTilId
        oppdragslinje.henvisning = "123456"
        return oppdragslinje
    }
}
