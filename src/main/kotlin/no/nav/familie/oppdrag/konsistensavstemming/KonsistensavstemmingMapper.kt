package no.nav.familie.oppdrag.konsistensavstemming

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.oppdrag.avstemming.AvstemmingMapper
import no.nav.familie.oppdrag.avstemming.SystemKode
import no.nav.familie.oppdrag.iverksetting.OppdragSkjemaConstants
import no.nav.familie.oppdrag.iverksetting.SatsTypeKode
import no.nav.familie.oppdrag.iverksetting.UtbetalingsfrekvensKode
import no.nav.virksomhet.tjenester.avstemming.informasjon.konsistensavstemmingsdata.v1.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class KonsistensavstemmingMapper(private val fagsystem: String,
                                 private val utbetalingsoppdrag: List<Utbetalingsoppdrag>,
                                 private val avstemmingsDato: LocalDateTime) {
    private val tidspunktFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")
    private val datoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val avstemmingId = AvstemmingMapper().encodeUUIDBase64(UUID.randomUUID())
    var totalBeløp = 0L
    var totalantall = 0

    fun lagAvstemmingsmeldinger() : List<Konsistensavstemmingsdata>  {
        return (listOf(lagStartmelding()) + lagDatameldinger() + listOf(lagSluttmelding()))
    }

    private fun lagStartmelding() = lagAksjonsmelding(KonsistensavstemmingConstants.START)

    private fun lagSluttmelding() = lagAksjonsmelding(KonsistensavstemmingConstants.AVSLUTT)

    private fun lagDatameldinger(): List<Konsistensavstemmingsdata> {
        val dataListe: MutableList<Konsistensavstemmingsdata> = arrayListOf()

        for (utbetalingsoppdrag in utbetalingsoppdrag) {
            val konsistensavstemmingsdata = lagAksjonsmelding(KonsistensavstemmingConstants.DATA)
            konsistensavstemmingsdata.apply {
                oppdragsdataListe.add(lagOppdragsdata(utbetalingsoppdrag))

            }
            dataListe.add(konsistensavstemmingsdata)
        }
        // legger til totaldata på slutten
        dataListe.add(lagTotaldata())
        return dataListe
    }

    private fun lagOppdragsdata(utbetalingsoppdrag: Utbetalingsoppdrag): Oppdragsdata {
        return Oppdragsdata().apply {
            fagomradeKode = utbetalingsoppdrag.fagSystem
            fagsystemId = utbetalingsoppdrag.saksnummer
            utbetalingsfrekvens = UtbetalingsfrekvensKode.MÅNEDLIG.kode
            oppdragGjelderId = utbetalingsoppdrag.aktoer
            oppdragGjelderFom = OppdragSkjemaConstants.OPPDRAG_GJELDER_DATO_FOM.format(datoFormatter)
            saksbehandlerId = utbetalingsoppdrag.saksbehandlerId
            oppdragsenhetListe.add(lagEnhet())
            utbetalingsoppdrag.utbetalingsperiode.mapIndexed { index, periode ->
                oppdragslinjeListe.add(lagOppdragsLinjeListe(utbetalingsperiode = periode, utbetalingsoppdrag = utbetalingsoppdrag,
                        teller = (index+100)))
            }
        }
    }

    private fun lagOppdragsLinjeListe(utbetalingsperiode: Utbetalingsperiode, utbetalingsoppdrag: Utbetalingsoppdrag, teller: Int): Oppdragslinje {
        akkumulerTotalbeløp(utbetalingsperiode)
        return Oppdragslinje().apply {
            vedtakId = utbetalingsperiode.datoForVedtak.format(datoFormatter)
            delytelseId = utbetalingsoppdrag.saksnummer+teller.toString()
            klassifikasjonKode = utbetalingsperiode.klassifisering
            vedtakPeriode = Periode().apply {
                fom = utbetalingsperiode.vedtakdatoFom.format(datoFormatter)
                tom = utbetalingsperiode.vedtakdatoTom.format(datoFormatter)
            }
            sats = utbetalingsperiode.sats
            satstypeKode = SatsTypeKode.fromKode(utbetalingsperiode.satsType.name).kode
            brukKjoreplan = OppdragSkjemaConstants.BRUK_KJØREPLAN
            fradragTillegg = OppdragSkjemaConstants.FRADRAG_TILLEGG.value()
            saksbehandlerId = utbetalingsoppdrag.saksbehandlerId
            utbetalesTilId = utbetalingsperiode.utbetalesTil
            henvisning = utbetalingsperiode.behandlingId.toString()
            attestantListe.add(lagAttestant(utbetalingsoppdrag))
        }
    }

    private fun akkumulerTotalbeløp(utbetalingsperiode: Utbetalingsperiode) {
        // utlede om utbetalingsperioden er aktuell for avstemmingsdato
        if (utbetalingsperiode.vedtakdatoFom.isBefore(avstemmingsDato.toLocalDate()) && utbetalingsperiode.vedtakdatoTom.isAfter(avstemmingsDato.toLocalDate())) {
            totalBeløp+=utbetalingsperiode.sats.toLong()
            totalantall++
        }
    }

    private fun lagAttestant(utbetalingsoppdrag: Utbetalingsoppdrag): Attestant {
        return Attestant().apply {
            attestantId = utbetalingsoppdrag.saksbehandlerId
        }
    }

    private fun lagEnhet(): Enhet {
        return Enhet().apply {
            enhetType = OppdragSkjemaConstants.ENHET_TYPE
            enhet = OppdragSkjemaConstants.ENHET
            enhetFom = OppdragSkjemaConstants.ENHET_DATO_FOM.format(datoFormatter)
        }
    }

    private fun lagTotaldata(): Konsistensavstemmingsdata {
        val konsistensavstemmingsdata = lagAksjonsmelding(KonsistensavstemmingConstants.DATA)
        konsistensavstemmingsdata.apply {
            totaldata = Totaldata().apply {
                totalAntall = totalantall.toBigInteger()
                totalBelop = BigDecimal.valueOf(totalBeløp)
                fortegn = getFortegn(totalBeløp)
            }
        }
        return konsistensavstemmingsdata
    }

    private fun getFortegn(satsbeløp: Long): String {
        return if (BigDecimal.valueOf(satsbeløp) >= BigDecimal.ZERO) KonsistensavstemmingConstants.FORTEGN_T else KonsistensavstemmingConstants.FORTEGN_F
    }

    private fun lagAksjonsmelding(aksjontype: String): Konsistensavstemmingsdata =
            Konsistensavstemmingsdata().apply {
                aksjonsdata = opprettAksjonsdata(aksjontype)
            }

    private fun opprettAksjonsdata(aksjonstype: String): Aksjonsdata {
        return Aksjonsdata().apply {
            this.aksjonsType = aksjonstype
            this.kildeType = KonsistensavstemmingConstants.KILDETYPE
            this.avstemmingType = KonsistensavstemmingConstants.KONSISTENSAVSTEMMING
            this.avleverendeKomponentKode = fagsystem
            this.mottakendeKomponentKode = SystemKode.OPPDRAGSSYSTEMET.kode
            this.underkomponentKode = fagsystem
            this.tidspunktAvstemmingTom = avstemmingsDato.format(tidspunktFormatter)
            this.avleverendeAvstemmingId = avstemmingId
            this.brukerId = fagsystem
        }
    }
}