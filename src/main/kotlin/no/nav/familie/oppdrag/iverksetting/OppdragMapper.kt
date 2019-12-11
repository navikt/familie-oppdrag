package no.nav.familie.oppdrag.iverksetting

import no.nav.familie.ks.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.familie.ks.kontrakter.oppdrag.Utbetalingsperiode
import no.trygdeetaten.skjema.oppdrag.*
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class OppdragMapper {

    private val objectFactory = ObjectFactory()
    val tidspunktFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")

    fun tilOppdrag110(utbetalingsoppdrag: Utbetalingsoppdrag): Oppdrag110 {

        val avstemming = objectFactory.createAvstemming115().apply {
            nokkelAvstemming = utbetalingsoppdrag.avstemmingTidspunkt.format(tidspunktFormatter)
            kodeKomponent = utbetalingsoppdrag.fagSystem
            tidspktMelding = utbetalingsoppdrag.avstemmingTidspunkt.format(tidspunktFormatter)
        }

        val oppdragsEnhet = objectFactory.createOppdragsEnhet120().apply {
            enhet = OppdragSkjemaConstants.ENHET
            typeEnhet = OppdragSkjemaConstants.ENHET_TYPE
            datoEnhetFom = OppdragSkjemaConstants.ENHET_DATO_FOM.toXMLDate()
        }

        val oppdrag110 = objectFactory.createOppdrag110().apply {
            kodeAksjon = OppdragSkjemaConstants.KODE_AKSJON
            kodeEndring = EndringsKode.fromKode(utbetalingsoppdrag.kodeEndring.name).kode
            kodeFagomraade = utbetalingsoppdrag.fagSystem
            fagsystemId = utbetalingsoppdrag.saksnummer
            utbetFrekvens = UtbetalingsfrekvensKode.MÅNEDLIG.kode
            oppdragGjelderId = utbetalingsoppdrag.aktoer
            datoOppdragGjelderFom = OppdragSkjemaConstants.OPPDRAG_GJELDER_DATO_FOM.toXMLDate()
            saksbehId = utbetalingsoppdrag.saksbehandlerId
            avstemming115 = avstemming
            oppdragsEnhet120.add(oppdragsEnhet)
            utbetalingsoppdrag.utbetalingsperiode.forEach {
                oppdragsLinje150.add(tilOppdragsLinje150(utbetalingsperiode = it, utbetalingsoppdrag = utbetalingsoppdrag))
            }
        }

        return oppdrag110
    }

    private fun tilOppdragsLinje150(utbetalingsperiode: Utbetalingsperiode, utbetalingsoppdrag: Utbetalingsoppdrag): OppdragsLinje150 {

        val attestant = objectFactory.createAttestant180().apply {
            attestantId = utbetalingsoppdrag.saksbehandlerId
        }

        return objectFactory.createOppdragsLinje150().apply {
            kodeEndringLinje = if (EndringsKode.NY.equals(utbetalingsoppdrag.kodeEndring.name)) EndringsKode.NY.kode else EndringsKode.ENDRING.kode
            utbetalingsperiode.opphør?.let {
                kodeStatusLinje = TkodeStatusLinje.OPPH
                datoStatusFom = it.opphørDatoFom.toXMLDate()
            }
            vedtakId = utbetalingsperiode.datoForVedtak.toString()
            delytelseId = utbetalingsoppdrag.saksnummer
            kodeKlassifik = utbetalingsperiode.klassifisering
            datoVedtakFom = utbetalingsperiode.vedtakdatoFom.toXMLDate()
            datoVedtakTom = utbetalingsperiode.vedtakdatoTom.toXMLDate()
            sats = utbetalingsperiode.sats
            fradragTillegg = OppdragSkjemaConstants.FRADRAG_TILLEGG
            typeSats = SatsTypeKode.fromKode(utbetalingsperiode.satsType.name).kode
            brukKjoreplan = OppdragSkjemaConstants.BRUK_KJØREPLAN
            saksbehId = utbetalingsoppdrag.saksbehandlerId
            utbetalesTilId = utbetalingsperiode.utbetalesTil
            henvisning = utbetalingsperiode.behandlingId.toString()
            attestant180.add(attestant)
        }
    }

    fun tilOppdrag(oppdrag110: Oppdrag110): Oppdrag {
        return objectFactory.createOppdrag().apply {
            this.oppdrag110 = oppdrag110
        }
    }
}