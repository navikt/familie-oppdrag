package no.nav.familie.oppdrag.simulering.mock

import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaaDetaljer
import no.nav.system.os.entiteter.typer.simpletypes.KodeStatusLinje
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdrag
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdragslinje
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class SimuleringGeneratorTest {

    private val simuleringGenerator = SimuleringGenerator()

    @Test
    fun `Simulering av ny ytelse med positivt resultat over 6 måneder`() {
        val oppdragGjelderId = "12345678901"
        val kodeEndring = "NY"
        val request: SimulerBeregningRequest = opprettSimulerBeregningRequest(oppdragGjelderId, kodeEndring)
        request.request.oppdrag.oppdragslinje.add(opprettOppdragslinje("NY", null, 2339, oppdragGjelderId, "2020-06-01", "2020-11-30", null))

        val response = simuleringGenerator.opprettSimuleringsResultat(request)
        assertThat(response.response.simulering.gjelderId)
            .isEqualTo(oppdragGjelderId)
            .isEqualTo(response.response.simulering.beregningsPeriode[0].beregningStoppnivaa[0].utbetalesTilId)
            .withFailMessage("Forventet 'gjelderId' var feil og/eller mottaker av ytelsen stemte ikke overens med personen ytelsen gjelder")
        assertThat(response.response.simulering.beregningsPeriode.size)
            .isEqualTo(1)
            .withFailMessage("Resultatperioden ble delt opp feil, ettersom alle periodene er sammenhengende skal de komme som en lang resultatperiode")
        assertThat(response.response.simulering.beregningsPeriode[0].beregningStoppnivaa.size)
            .isEqualTo(6)
            .withFailMessage("Perioden fom 01.06.2020 - tom 30.11.2020 skal være på totat 5 måneder. Antall måneder i resultatet var: " + response.response.simulering.beregningsPeriode[0].beregningStoppnivaa.size)
        val detaljer: List<BeregningStoppnivaaDetaljer> =
            response.response.simulering.beregningsPeriode[0].beregningStoppnivaa[0].beregningStoppnivaaDetaljer
        detaljer.sortedBy { beregningStoppnivaaDetaljer -> beregningStoppnivaaDetaljer.behandlingskode }
        assertThat(detaljer.size)
            .isEqualTo(1)
            .withFailMessage("En positiv respons skal bare ha en beregningStoppnivaaDetaljer. Det at den hadde flere enn 1 indikerer at det feilaktig er oppstått andre posteringer")
    }

    @Test
    fun `Simulering av reduksjon av eksisterende ytelse fra 1330 kr pr måned til 1200 kr på måned`() {
        val oppdragGjelderId = "12345678902"
        val kodeEndring = "ENDR"
        val request: SimulerBeregningRequest = opprettSimulerBeregningRequest(oppdragGjelderId, kodeEndring)
        request.request.oppdrag.oppdragslinje.add(opprettOppdragslinje(kodeEndring, KodeStatusLinje.OPPH, 1330, oppdragGjelderId, "2020-07-01", "2020-11-30", "2020-07-01"))
        request.request.oppdrag.oppdragslinje.add(opprettOppdragslinje("NY", null, 1200, oppdragGjelderId, "2020-07-01", "2020-11-30", null))

        val response = simuleringGenerator.opprettSimuleringsResultat(request)
        assertThat(response.response.simulering.gjelderId)
            .isEqualTo(oppdragGjelderId)
            .isEqualTo(response.response.simulering.beregningsPeriode[0].beregningStoppnivaa[0].utbetalesTilId)
            .withFailMessage("Forventet 'gjelderId' var feil og/eller mottaker av ytelsen stemte ikke overens med personen ytelsen gjelder")
        assertThat(response.response.simulering.beregningsPeriode[0].beregningStoppnivaa.size)
            .isEqualTo(5)
            .withFailMessage("Perioden fom 01.07.2020 - tom 30.11.2020 skal være på totat 5 måneder. Antall måneder i resultatet var: " + response.response.simulering.beregningsPeriode[0].beregningStoppnivaa.size)

        val detaljer: List<BeregningStoppnivaaDetaljer> =
            response.response.simulering.beregningsPeriode[0].beregningStoppnivaa[0].beregningStoppnivaaDetaljer
        assertThat(detaljer.size)
            .isEqualTo(3)
            .withFailMessage("En respons med senket ytelse skal ha 3 beregningsStoppnivåDetaljer, Ytelse, Feilutbetaling og Negativ ytelse")
        detaljer.sortedBy { beregningStoppnivaaDetaljer -> beregningStoppnivaaDetaljer.behandlingskode }
        assertThat(detaljer[0].typeKlasse)
            .isEqualTo("YTEL")
            .withFailMessage("Forventet typeKlasse YTEL men fikk: " + detaljer[0].typeKlasse)
        assertThat(detaljer[0].belop)
            .isEqualTo(BigDecimal.valueOf(1200))
            .withFailMessage("Beløp for ny ytelse var feil, forventet månedsbeløp 1200 men fikk: " + detaljer[0].belop)
        assertThat(detaljer[1].typeKlasse)
            .isEqualTo("FEIL")
            .withFailMessage("Forventet typeKlasse FEIL men fikk: " + detaljer[1].typeKlasse)
        assertThat(detaljer[1].belop)
            .isEqualTo(BigDecimal.valueOf(130))
            .withFailMessage("Beløp for feilutbetaling var feil, forventet 130 men fikk: " + detaljer[1].belop)
        assertThat(detaljer[2].typeKlasse)
            .isEqualTo("YTEL")
            .withFailMessage("Forventet typeKlasse YTEL men fikk: " + detaljer[2].typeKlasse)
        assertThat(detaljer[2].belop)
            .isEqualTo(BigDecimal.valueOf(-1330))
            .withFailMessage("Beløp for gammel ytelse var feil, forventet månedsbeløp 1330 men fikk: " + detaljer[2].belop)
    }

    @Test
    fun `Simulering av opphør av ytelse som gir negativt resultat`() {
        val oppdragGjelderId = "12345678903"
        val kodeEndring = "ENDR"
        val request: SimulerBeregningRequest = opprettSimulerBeregningRequest(oppdragGjelderId, kodeEndring)
        request.request.oppdrag.oppdragslinje.add(opprettOppdragslinje(kodeEndring, KodeStatusLinje.OPPH, 1330, oppdragGjelderId, "2020-07-01", "2020-10-31", "2020-07-01"))

        val response = simuleringGenerator.opprettSimuleringsResultat(request)
        assertThat(response.response.simulering.gjelderId)
            .isEqualTo(oppdragGjelderId)
            .isEqualTo(response.response.simulering.beregningsPeriode[0].beregningStoppnivaa[0].utbetalesTilId)
            .withFailMessage("Forventet 'gjelderId' var feil og/eller mottaker av ytelsen stemte ikke overens med personen ytelsen gjelder")
        assertThat(response.response.simulering.beregningsPeriode[0].beregningStoppnivaa.size)
            .isEqualTo(4)
            .withFailMessage("Perioden fom 01.07.2020 - tom 31.10.2020 skal være på totat 4 måneder. Antall måneder i resultatet var: " + response.response.simulering.beregningsPeriode[0].beregningStoppnivaa.size)

        val detaljer: List<BeregningStoppnivaaDetaljer> =
            response.response.simulering.beregningsPeriode[0].beregningStoppnivaa[0].beregningStoppnivaaDetaljer
        assertThat(detaljer.size)
            .isEqualTo(3)
            .withFailMessage("En respons med senket ytelse skal ha 3 beregningsStoppnivåDetaljer, Ytelse, Feilutbetaling og Negativ ytelse")
        detaljer.sortedBy { beregningStoppnivaaDetaljer -> beregningStoppnivaaDetaljer.behandlingskode }
        assertThat(detaljer[0].typeKlasse)
            .isEqualTo("YTEL")
            .withFailMessage("Forventet typeKlasse YTEL men fikk: " + detaljer[0].typeKlasse)
        assertThat(detaljer[0].belop)
            .isEqualTo(BigDecimal.valueOf(1330))
            .withFailMessage("Beløp for  ytelse var feil, forventet månedsbeløp 1330 men fikk: " + detaljer[0].belop)
        assertThat(detaljer[1].typeKlasse)
            .isEqualTo("FEIL")
            .withFailMessage("Forventet typeKlasse FEIL men fikk: " + detaljer[1].typeKlasse)
        assertThat(detaljer[1].belop)
            .isEqualTo(BigDecimal.valueOf(1330))
            .withFailMessage("Beløp for feilutbetaling var feil, forventet 1330 men fikk: " + detaljer[1].belop)
        assertThat(detaljer[2].typeKlasse)
            .isEqualTo("YTEL")
            .withFailMessage("Forventet typeKlasse YTEL men fikk: " + detaljer[2].typeKlasse)
        assertThat(detaljer[2].belop)
            .isEqualTo(BigDecimal.valueOf(-1330))
            .withFailMessage("Beløp for gammel ytelse var feil, forventet månedsbeløp 1330 men fikk: " + detaljer[2].belop)
    }

    private fun opprettSimulerBeregningRequest(oppdragGjelderId: String, kodeEndring: String): SimulerBeregningRequest {
        val request = SimulerBeregningRequest()
        request.request =
            no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningRequest()
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
        datoStatusFom: String?
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
