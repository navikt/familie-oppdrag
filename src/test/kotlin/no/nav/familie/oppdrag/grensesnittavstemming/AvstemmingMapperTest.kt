package no.nav.familie.oppdrag.grensesnittavstemming

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.repository.somOppdragLager
import no.nav.familie.oppdrag.util.TestOppdragMedAvstemmingsdato
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class AvstemmingMapperTest {
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
        val utbetalingsoppdrag = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(LocalDateTime.now().minusDays(1).withHour(13), fagområde)
        val oppdragLager = utbetalingsoppdrag.somOppdragLager
        val mapper = AvstemmingMapper(listOf(oppdragLager), fagområde)
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(3, meldinger.size)
        assertAksjon(utbetalingsoppdrag, AksjonType.START, meldinger.first().aksjon)
        assertAksjon(utbetalingsoppdrag, AksjonType.DATA, meldinger[1].aksjon)
        assertAksjon(utbetalingsoppdrag, AksjonType.AVSL, meldinger.last().aksjon)

        assertDetaljData(utbetalingsoppdrag, meldinger[1].detalj.first())
        assertTotalData(utbetalingsoppdrag, meldinger[1].total)
        assertPeriodeData(utbetalingsoppdrag, meldinger[1].periode)
        assertGrunnlagsdata(utbetalingsoppdrag, meldinger[1].grunnlag)
    }

    @Test
    fun testerAtFomOgTomBlirSattRiktigVedGrensesnittavstemming() {
        val baOppdragLager1 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(LocalDateTime.now().minusDays(1).withHour(13), fagområde).somOppdragLager
        val baOppdragLager2 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(LocalDateTime.now().minusDays(1).withHour(15), fagområde).somOppdragLager
        val mapper = AvstemmingMapper(listOf(baOppdragLager1, baOppdragLager2), fagområde)
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(3, meldinger.size)
        assertEquals(baOppdragLager1.avstemmingTidspunkt.format(tidspunktFormatter), meldinger.first().aksjon.nokkelFom)
        assertEquals(baOppdragLager2.avstemmingTidspunkt.format(tidspunktFormatter), meldinger.first().aksjon.nokkelTom)
    }

    fun assertAksjon(utbetalingsoppdrag: Utbetalingsoppdrag, expected: AksjonType, actual: Aksjonsdata) {
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

    fun assertDetaljData(utbetalingsoppdrag: Utbetalingsoppdrag, actual: Detaljdata) {
        assertEquals(DetaljType.MANG, actual.detaljType)
        assertEquals(utbetalingsoppdrag.aktoer, actual.offnr)
        assertEquals(fagområde, actual.avleverendeTransaksjonNokkel)
        assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(tidspunktFormatter), actual.tidspunkt)
        assertEquals(null, actual.meldingKode)
        assertEquals(null, actual.alvorlighetsgrad)
        assertEquals(null, actual.tekstMelding)
    }

    fun assertTotalData(utbetalingsoppdrag: Utbetalingsoppdrag, actual: Totaldata) {
        assertEquals(1, actual.totalAntall)
        assertEquals(utbetalingsoppdrag.utbetalingsperiode.first().sats, actual.totalBelop)
        assertEquals(Fortegn.T, actual.fortegn)
    }

    fun assertPeriodeData(utbetalingsoppdrag: Utbetalingsoppdrag, actual: Periodedata) {
        assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(DateTimeFormatter.ofPattern("yyyyMMddHH")),
                actual.datoAvstemtFom)
        assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(DateTimeFormatter.ofPattern("yyyyMMddHH")),
                actual.datoAvstemtTom)
    }

    fun assertGrunnlagsdata(utbetalingsoppdrag: Utbetalingsoppdrag, actual: Grunnlagsdata) {
        assertEquals(1, actual.manglerAntall)
        assertEquals(utbetalingsoppdrag.utbetalingsperiode.first().sats, actual.manglerBelop)
        assertEquals(Fortegn.T, actual.manglerFortegn)

        assertEquals(0, actual.godkjentAntall)
        assertEquals(BigDecimal.ZERO, actual.godkjentBelop)
        assertEquals(Fortegn.T, actual.godkjentFortegn)

        assertEquals(0, actual.avvistAntall)
        assertEquals(BigDecimal.ZERO, actual.avvistBelop)
        assertEquals(Fortegn.T, actual.avvistFortegn)
    }

}