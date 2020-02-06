package no.nav.familie.oppdrag.konsistensavstemming

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.avstemming.SystemKode
import no.nav.familie.oppdrag.iverksetting.OppdragSkjemaConstants
import no.nav.familie.oppdrag.iverksetting.SatsTypeKode
import no.nav.familie.oppdrag.iverksetting.UtbetalingsfrekvensKode
import no.nav.familie.oppdrag.util.TestOppdragMedAvstemmingsdato
import no.nav.virksomhet.tjenester.avstemming.informasjon.konsistensavstemmingsdata.v1.*
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class KonsistensavstemmingMapperTest {
    val fagområde = "BA"
    val idag = LocalDateTime.now()
    val tidspunktFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")
    val datoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")


    @Test
    fun tester_at_det_mappes_riktig_til_konsistensavstemming() {
        val utbetalingsoppdrag = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(idag, fagområde)
        val mapper = KonsistensavstemmingMapper(fagområde, listOf(utbetalingsoppdrag), idag)
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(4, meldinger.size)
        // START-meldingen
        assertAksjon(KonsistensavstemmingConstants.START, meldinger.first().aksjonsdata)
        // DATA-meldingen
        assertAksjon(KonsistensavstemmingConstants.DATA, meldinger[1].aksjonsdata)
        assertOppdragsdata(utbetalingsoppdrag, meldinger[1].oppdragsdataListe.first())
        // TOTALDATA-meldingen
        assertAksjon(KonsistensavstemmingConstants.DATA, meldinger[2].aksjonsdata)
        assertTotaldata(utbetalingsoppdrag.utbetalingsperiode.first(), meldinger[2].totaldata)
        // AVSLUTT-meldingen
        assertAksjon(KonsistensavstemmingConstants.AVSLUTT, meldinger.last().aksjonsdata)
    }

    @Test
    fun totaldata_skal_akkumuleres_riktig() {
        val utbetalingsoppdrag = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(idag, fagområde)
        val utbetalingsoppdrag2 = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(idag, fagområde)
        val mapper = KonsistensavstemmingMapper(fagområde, listOf(utbetalingsoppdrag, utbetalingsoppdrag2), idag)
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(5, meldinger.size)
        assertEquals(KonsistensavstemmingConstants.DATA, meldinger[3].aksjonsdata.aksjonsType)
        assertEquals(BigInteger.TWO, meldinger[3].totaldata.totalAntall)
    }

    fun assertAksjon(expected: String, actual: Aksjonsdata) {
        assertEquals(expected, actual.aksjonsType)
        assertEquals(KonsistensavstemmingConstants.KILDETYPE, actual.kildeType)
        assertEquals(KonsistensavstemmingConstants.KONSISTENSAVSTEMMING, actual.avstemmingType)
        assertEquals(fagområde, actual.avleverendeKomponentKode)
        assertEquals(SystemKode.OPPDRAGSSYSTEMET.kode, actual.mottakendeKomponentKode)
        assertEquals(fagområde, actual.underkomponentKode)
        assertEquals(idag.format(tidspunktFormatter), actual.tidspunktAvstemmingTom)
        assertEquals(fagområde, actual.brukerId)
    }

    fun assertTotaldata(utbetalingsperiode: Utbetalingsperiode, actual: Totaldata) {
        assertEquals(BigInteger.ONE, actual.totalAntall)
        assertEquals(utbetalingsperiode.sats, actual.totalBelop)
        assertEquals(KonsistensavstemmingConstants.FORTEGN_T, actual.fortegn)
    }

    fun assertOppdragsdata(utbetalingsoppdrag: Utbetalingsoppdrag, actual: Oppdragsdata) {
        assertEquals(fagområde, actual.fagomradeKode)
        assertEquals(utbetalingsoppdrag.saksnummer, actual.fagsystemId)
        assertEquals(UtbetalingsfrekvensKode.MÅNEDLIG.kode, actual.utbetalingsfrekvens)
        assertEquals(utbetalingsoppdrag.aktoer, actual.oppdragGjelderId)
        assertEquals(OppdragSkjemaConstants.OPPDRAG_GJELDER_DATO_FOM.format(datoFormatter), actual.oppdragGjelderFom)
        assertEquals(utbetalingsoppdrag.saksbehandlerId, actual.saksbehandlerId)
        assertEnhet(actual.oppdragsenhetListe.first())
        assertOppdragsLinjeListe(utbetalingsoppdrag.utbetalingsperiode.first(), utbetalingsoppdrag.saksbehandlerId, actual.oppdragslinjeListe.first())
    }

    fun assertOppdragsLinjeListe(utbetalingsperiode: Utbetalingsperiode, saksbehandler: String, actual: Oppdragslinje) {
        assertEquals(utbetalingsperiode.datoForVedtak.format(datoFormatter), actual.vedtakId)
        assertEquals(utbetalingsperiode.klassifisering, actual.klassifikasjonKode)
        assertEquals(utbetalingsperiode.vedtakdatoFom.format(datoFormatter), actual.vedtakPeriode.fom)
        assertEquals(utbetalingsperiode.vedtakdatoTom.format(datoFormatter), actual.vedtakPeriode.tom)
        assertEquals(utbetalingsperiode.sats, actual.sats)
        assertEquals(SatsTypeKode.fromKode(utbetalingsperiode.satsType.name).kode, actual.satstypeKode)
        assertEquals(OppdragSkjemaConstants.BRUK_KJØREPLAN, actual.brukKjoreplan)
        assertEquals(OppdragSkjemaConstants.FRADRAG_TILLEGG.value(), actual.fradragTillegg)
        assertEquals(saksbehandler, actual.saksbehandlerId)
        assertEquals(utbetalingsperiode.utbetalesTil, actual.utbetalesTilId)
        assertEquals(utbetalingsperiode.behandlingId.toString(), actual.henvisning)
        assertEquals(saksbehandler, actual.attestantListe.first().attestantId)
    }

    fun assertEnhet(enhet: Enhet) {
        assertEquals(OppdragSkjemaConstants.ENHET_TYPE, enhet.enhetType)
        assertEquals(OppdragSkjemaConstants.ENHET, enhet.enhet)
        assertEquals(OppdragSkjemaConstants.ENHET_DATO_FOM.format(datoFormatter), enhet.enhetFom)
    }

}