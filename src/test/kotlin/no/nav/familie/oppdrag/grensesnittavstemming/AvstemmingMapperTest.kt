package no.nav.familie.oppdrag.grensesnittavstemming

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class AvstemmingMapperTest {
    val idag = LocalDate.now()
    val fagområde = "BA"
    val tidspunktFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")

    @Test
    fun testMappingAvTomListe() {
        val mapper = AvstemmingMapper(emptyList(), fagområde)
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(0, meldinger.size)
    }

    @Test
    fun testMappingTilGrensesnittavstemming() {
        val oppdragProtokoll = lagOppdragProtokoll()
        val mapper = AvstemmingMapper(listOf(oppdragProtokoll), fagområde)
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(3, meldinger.size)
        assertAksjon(AksjonType.START, meldinger.first().aksjon)
        assertAksjon(AksjonType.DATA, meldinger[1].aksjon)
        assertAksjon(AksjonType.AVSL, meldinger.last().aksjon)

        assertDetaljData(meldinger[1].detalj.first())

        assertTotalData(meldinger[1].total)
        assertPeriodeData(meldinger[1].periode)
        assertGrunnlagsdata(meldinger[1].grunnlag)
    }

    fun assertAksjon(expected: AksjonType, actual: Aksjonsdata) {
        assertEquals(expected, actual.aksjonType)
        assertEquals(KildeType.AVLEV, actual.kildeType)
        assertEquals(AvstemmingType.GRSN, actual.avstemmingType)
        assertEquals(fagområde, actual.avleverendeKomponentKode)
        assertEquals(SystemKode.OPPDRAGSSYSTEMET.kode, actual.mottakendeKomponentKode)
        assertEquals(fagområde, actual.underkomponentKode)
        assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(tidspunktFormatter), actual.nokkelFom)
        assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(tidspunktFormatter), actual.nokkelTom)
        assertEquals(fagområde, actual.brukerId)
    }

    fun assertDetaljData(actual: Detaljdata) {
        assertEquals(DetaljType.MANG, actual.detaljType)
        assertEquals(utbetalingsoppdrag.aktoer, actual.offnr)
        assertEquals(fagområde, actual.avleverendeTransaksjonNokkel)
        assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(tidspunktFormatter), actual.tidspunkt)
        assertEquals(null, actual.meldingKode)
        assertEquals(null, actual.alvorlighetsgrad)
        assertEquals(null, actual.tekstMelding)
    }

    fun assertTotalData(actual: Totaldata) {
        assertEquals(1, actual.totalAntall)
        assertEquals(utbetalingsoppdrag.utbetalingsperiode.first().sats, actual.totalBelop)
        assertEquals(Fortegn.T, actual.fortegn)
    }

    fun assertPeriodeData(actual: Periodedata) {
        assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(DateTimeFormatter.ofPattern("yyyyMMddHH")),
                actual.datoAvstemtFom)
        assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(DateTimeFormatter.ofPattern("yyyyMMddHH")),
                actual.datoAvstemtTom)
    }

    fun assertGrunnlagsdata(actual: Grunnlagsdata) {
        assertEquals(1, actual.manglerAntall)
        assertEquals(utbetalingsperiode1.sats, actual.manglerBelop)
        assertEquals(Fortegn.T, actual.manglerFortegn)

        assertEquals(0, actual.godkjentAntall)
        assertEquals(BigDecimal.ZERO, actual.godkjentBelop)
        assertEquals(Fortegn.T, actual.godkjentFortegn)

        assertEquals(0, actual.avvistAntall)
        assertEquals(BigDecimal.ZERO, actual.avvistBelop)
        assertEquals(Fortegn.T, actual.avvistFortegn)
    }


    fun lagOppdragProtokoll() : OppdragProtokoll {
        val oppdrag = OppdragMapper().tilOppdrag(oppdrag110)
        return OppdragProtokoll.lagFraOppdrag(utbetalingsoppdrag, oppdrag)
    }

    val utbetalingsperiode1 = Utbetalingsperiode(
            erEndringPåEksisterendePeriode = false,
            opphør = null,
            datoForVedtak = idag,
            klassifisering = "BATR",
            vedtakdatoFom = idag,
            vedtakdatoTom = idag.plusYears(6),
            sats = BigDecimal.valueOf(1354L),
            satsType = Utbetalingsperiode.SatsType.MND,
            utbetalesTil = "12345678911",
            behandlingId = 987654321L
    )

    val utbetalingsoppdrag = Utbetalingsoppdrag(
            kodeEndring = Utbetalingsoppdrag.KodeEndring.NY,
            fagSystem = "BA",
            saksnummer = "12345678",
            aktoer = "12345678911",
            saksbehandlerId = "Z992991",
            utbetalingsperiode = listOf(utbetalingsperiode1)
    )

    val oppdrag110 = OppdragMapper().tilOppdrag110(utbetalingsoppdrag)

}