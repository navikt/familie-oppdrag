package no.nav.familie.oppdrag.iverksetting

import no.nav.familie.ks.kontrakter.oppdrag.Opphør
import no.nav.familie.ks.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.familie.ks.kontrakter.oppdrag.Utbetalingsperiode
import no.trygdeetaten.skjema.oppdrag.Oppdrag110
import no.trygdeetaten.skjema.oppdrag.OppdragsLinje150
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import java.math.BigDecimal
import java.time.LocalDate

class KontraktTilOppdragTest {
    val idag = LocalDate.now()

    @Test
    fun mappeVedtaketTilMariBerg() {

        val utbetalingsperiode1 = Utbetalingsperiode(
                erEndringPåEksisterendePeriode = false,
                opphør = null,
                datoForVedtak = idag,
                klassifisering = "BAOROSMS",
                vedtakdatoFom = idag,
                vedtakdatoTom = idag.plusYears(6),
                sats = BigDecimal.valueOf(1354L),
                satsType = Utbetalingsperiode.SatsType.MND,
                utbetalesTil = "12345678911",
                behandlingId = 987654321L
        )

        val utbetalingsperiode2 = Utbetalingsperiode(
                erEndringPåEksisterendePeriode = false,
                opphør = null,
                datoForVedtak = idag,
                klassifisering = "BAOROSMS",
                vedtakdatoFom = idag.plusYears(6).plusMonths(1),
                vedtakdatoTom = idag.plusYears(12).plusMonths(1),
                sats = BigDecimal.valueOf(1054L),
                satsType = Utbetalingsperiode.SatsType.MND,
                utbetalesTil = "12345678911",
                behandlingId = 987654321L
        )

        val utbetalingsoppdrag = Utbetalingsoppdrag(
                kodeEndring = Utbetalingsoppdrag.KodeEndring.NY,
                fagSystem = "IT05",
                saksnummer = "12345678",
                aktoer = "12345678911",
                saksbehandlerId = "Z992991",
                utbetalingsperiode = listOf(utbetalingsperiode1, utbetalingsperiode2)
        )

        val oppdrag110 = OppdragMapper().tilOppdrag110(utbetalingsoppdrag)

        assertOppdrag110(utbetalingsoppdrag, oppdrag110)
        assertOppdragslinje150(utbetalingsperiode1, utbetalingsoppdrag, oppdrag110.oppdragsLinje150.get(0))
        assertOppdragslinje150(utbetalingsperiode2, utbetalingsoppdrag, oppdrag110.oppdragsLinje150.get(1))
    }

    @Test
    fun mappeOpphørPåVedtaketTilMariBerg() {

        val utbetalingsperiode1 = Utbetalingsperiode(
                erEndringPåEksisterendePeriode = true,
                opphør = Opphør(idag.plusMonths(1)),
                datoForVedtak = idag,
                klassifisering = "BAOROSMS",
                vedtakdatoFom = idag,
                vedtakdatoTom = idag.plusYears(2),
                sats = BigDecimal.valueOf(1354L),
                satsType = Utbetalingsperiode.SatsType.MND,
                utbetalesTil = "12345678911",
                behandlingId = 987654321L
        )
        val utbetalingsoppdrag = Utbetalingsoppdrag(
                kodeEndring = Utbetalingsoppdrag.KodeEndring.ENDR,
                fagSystem = "IT05",
                saksnummer = "12345678",
                aktoer = "12345678911",
                saksbehandlerId = "Z992991",
                utbetalingsperiode = listOf(utbetalingsperiode1)
        )

        val oppdrag110 = OppdragMapper().tilOppdrag110(utbetalingsoppdrag)

        assertOppdrag110(utbetalingsoppdrag, oppdrag110)
        assertOppdragslinje150(utbetalingsperiode1, utbetalingsoppdrag, oppdrag110.oppdragsLinje150.get(0))
    }

    fun assertOppdrag110(utbetalingsoppdrag: Utbetalingsoppdrag, oppdrag110: Oppdrag110) {
        Assertions.assertEquals(OppdragSkjemaConstants.KODE_AKSJON, oppdrag110.kodeAksjon)
        Assertions.assertEquals(utbetalingsoppdrag.kodeEndring.name, oppdrag110.kodeEndring.toString())
        Assertions.assertEquals(utbetalingsoppdrag.fagSystem, oppdrag110.kodeFagomraade)
        Assertions.assertEquals(utbetalingsoppdrag.saksnummer, oppdrag110.fagsystemId)
        Assertions.assertEquals(UtbetalingsfrekvensKode.MÅNEDLIG.kode, oppdrag110.utbetFrekvens)
        Assertions.assertEquals(utbetalingsoppdrag.aktoer, oppdrag110.oppdragGjelderId)
        Assertions.assertEquals(OppdragSkjemaConstants.OPPDRAG_GJELDER_DATO_FOM.toXMLDate(), oppdrag110.datoOppdragGjelderFom)
        Assertions.assertEquals(utbetalingsoppdrag.saksbehandlerId, oppdrag110.saksbehId)
        Assertions.assertEquals(utbetalingsoppdrag.fagSystem, oppdrag110.avstemming115.kodeKomponent)
        Assertions.assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(OppdragMapper().tidspunktFormatter), oppdrag110.avstemming115.nokkelAvstemming)
        Assertions.assertEquals(utbetalingsoppdrag.avstemmingTidspunkt.format(OppdragMapper().tidspunktFormatter), oppdrag110.avstemming115.tidspktMelding)
        Assertions.assertEquals(OppdragSkjemaConstants.ENHET_TYPE, oppdrag110.oppdragsEnhet120.get(0).typeEnhet)
        Assertions.assertEquals(OppdragSkjemaConstants.ENHET, oppdrag110.oppdragsEnhet120.get(0).enhet)
        Assertions.assertEquals(OppdragSkjemaConstants.ENHET_DATO_FOM.toXMLDate(), oppdrag110.oppdragsEnhet120.get(0).datoEnhetFom)
    }

    fun assertOppdragslinje150(utbetalingsperiode: Utbetalingsperiode, utbetalingsoppdrag: Utbetalingsoppdrag, oppdragsLinje150: OppdragsLinje150) {
        Assertions.assertEquals(if (utbetalingsperiode.erEndringPåEksisterendePeriode) EndringsKode.ENDRING.kode else EndringsKode.NY.kode, oppdragsLinje150.kodeEndringLinje)
        assertOpphør(utbetalingsperiode, oppdragsLinje150)
        Assertions.assertEquals(utbetalingsperiode.datoForVedtak.toString(), oppdragsLinje150.vedtakId)
        Assertions.assertEquals(utbetalingsoppdrag.saksnummer, oppdragsLinje150.delytelseId)
        Assertions.assertEquals(utbetalingsperiode.klassifisering, oppdragsLinje150.kodeKlassifik)
        Assertions.assertEquals(utbetalingsperiode.vedtakdatoFom.toXMLDate(), oppdragsLinje150.datoVedtakFom)
        Assertions.assertEquals(utbetalingsperiode.vedtakdatoTom.toXMLDate(), oppdragsLinje150.datoVedtakTom)
        Assertions.assertEquals(utbetalingsperiode.sats, oppdragsLinje150.sats)
        Assertions.assertEquals(OppdragSkjemaConstants.FRADRAG_TILLEGG, oppdragsLinje150.fradragTillegg)
        Assertions.assertEquals(utbetalingsperiode.satsType.name, oppdragsLinje150.typeSats)
        Assertions.assertEquals(OppdragSkjemaConstants.BRUK_KJØREPLAN, oppdragsLinje150.brukKjoreplan)
        Assertions.assertEquals(utbetalingsoppdrag.saksbehandlerId, oppdragsLinje150.saksbehId)
        Assertions.assertEquals(utbetalingsoppdrag.aktoer, oppdragsLinje150.utbetalesTilId)
        Assertions.assertEquals(utbetalingsperiode.behandlingId.toString(), oppdragsLinje150.henvisning)
        Assertions.assertEquals(utbetalingsoppdrag.saksbehandlerId, oppdragsLinje150.attestant180.get(0).attestantId)
    }

    fun assertOpphør(utbetalingsperiode: Utbetalingsperiode, oppdragsLinje150: OppdragsLinje150) {
        if (utbetalingsperiode.opphør == null) {
            Assertions.assertEquals(utbetalingsperiode.opphør, oppdragsLinje150.kodeStatusLinje)
            Assertions.assertEquals(utbetalingsperiode.opphør, oppdragsLinje150.datoStatusFom)
        } else {
            utbetalingsperiode.opphør?.let {
                Assertions.assertEquals("OPPH", oppdragsLinje150.kodeStatusLinje.name)
                Assertions.assertEquals(it.opphørDatoFom.toXMLDate(), oppdragsLinje150.datoStatusFom)
            }
        }
    }
}