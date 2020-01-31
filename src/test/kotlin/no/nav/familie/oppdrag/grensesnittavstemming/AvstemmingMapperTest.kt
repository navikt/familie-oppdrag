package no.nav.familie.oppdrag.grensesnittavstemming

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.repository.somOppdragLager
import no.nav.familie.oppdrag.util.TestOppdragMedAvstemmingsdato
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class AvstemmingMapperTest {
    val fagområde = "BA"
    val tidspunktFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")

    @Test
    fun testMappingAvTomListe() {
        val mapper = AvstemmingMapper(emptyList(), fagområde, LocalDateTime.now(), LocalDateTime.now())
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(0, meldinger.size)
    }

    @Test
    fun testMappingTilGrensesnittavstemming() {
        val avstemmingstidspunkt = LocalDateTime.now().minusDays(1).withHour(13)
        val avstemmingFom = avstemmingstidspunkt.toLocalDate().atStartOfDay()
        val avstemmingTom = avstemmingstidspunkt.toLocalDate().atTime(LocalTime.MAX)
        val utbetalingsoppdrag = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(avstemmingstidspunkt, fagområde)
        val oppdragLager = utbetalingsoppdrag.somOppdragLager
        val mapper = AvstemmingMapper(listOf(oppdragLager), fagområde, avstemmingFom, avstemmingTom)
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(3, meldinger.size)
        assertAksjon(avstemmingFom, avstemmingTom, AksjonType.START, meldinger.first().aksjon)
        assertAksjon(avstemmingFom, avstemmingTom, AksjonType.DATA, meldinger[1].aksjon)
        assertAksjon(avstemmingFom, avstemmingTom, AksjonType.AVSL, meldinger.last().aksjon)

        assertDetaljData(utbetalingsoppdrag, meldinger[1].detalj.first())
        assertTotalData(utbetalingsoppdrag, meldinger[1].total)
        assertPeriodeData(utbetalingsoppdrag, meldinger[1].periode)
        assertGrunnlagsdata(utbetalingsoppdrag, meldinger[1].grunnlag)
    }

    @Test
    fun testerAtFomOgTomBlirSattRiktigVedGrensesnittavstemming() {
        val førsteAvstemmingstidspunkt = LocalDateTime.now().minusDays(1).withHour(13)
        val andreAvstemmingstidspunkt = LocalDateTime.now().minusDays(1).withHour(15)
        val avstemmingFom = førsteAvstemmingstidspunkt.toLocalDate().atStartOfDay()
        val avstemmingTom = andreAvstemmingstidspunkt.toLocalDate().atTime(LocalTime.MAX)
        val baOppdragLager1 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(førsteAvstemmingstidspunkt, fagområde).somOppdragLager
        val baOppdragLager2 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(andreAvstemmingstidspunkt, fagområde).somOppdragLager
        val mapper = AvstemmingMapper(listOf(baOppdragLager1, baOppdragLager2), fagområde, avstemmingFom, avstemmingTom)
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(3, meldinger.size)
        assertEquals(avstemmingFom.format(tidspunktFormatter), meldinger.first().aksjon.nokkelFom)
        assertEquals(avstemmingTom.format(tidspunktFormatter), meldinger.first().aksjon.nokkelTom)
    }

    fun assertAksjon(avstemmingFom: LocalDateTime, avstemmingTom: LocalDateTime,
                     expected: AksjonType, actual: Aksjonsdata) {
        assertEquals(expected, actual.aksjonType)
        assertEquals(KildeType.AVLEV, actual.kildeType)
        assertEquals(AvstemmingType.GRSN, actual.avstemmingType)
        assertEquals(fagområde, actual.avleverendeKomponentKode)
        assertEquals(SystemKode.OPPDRAGSSYSTEMET.kode, actual.mottakendeKomponentKode)
        assertEquals(fagområde, actual.underkomponentKode)
        assertEquals(avstemmingFom.format(tidspunktFormatter), actual.nokkelFom)
        assertEquals(avstemmingTom.format(tidspunktFormatter), actual.nokkelTom)
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