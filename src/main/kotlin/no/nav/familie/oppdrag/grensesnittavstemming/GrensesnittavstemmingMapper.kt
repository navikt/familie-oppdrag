package no.nav.familie.oppdrag.grensesnittavstemming

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.oppdrag.avstemming.AvstemmingMapper
import no.nav.familie.oppdrag.avstemming.AvstemmingMapper.fagområdeTilAvleverendeKomponentKode
import no.nav.familie.oppdrag.avstemming.SystemKode
import no.nav.familie.oppdrag.repository.OppdragLager
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.AksjonType
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Aksjonsdata
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.AvstemmingType
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Avstemmingsdata
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.DetaljType
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Detaljdata
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Fortegn
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Grunnlagsdata
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.KildeType
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Periodedata
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Totaldata
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class GrensesnittavstemmingMapper(
    private val fagområde: String,
    private val fom: LocalDateTime,
    private val tom: LocalDateTime,
) {

    private val ANTALL_DETALJER_PER_MELDING = 70
    private val tidspunktFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")
    val avstemmingId = AvstemmingMapper.encodeUUIDBase64(UUID.randomUUID())

    private val grunnlagsdata = Grunnlag()
    private val total = Total()
    private val avstemmingstidspunkt = Avstemmingstidspunkt()

    fun lagStartmelding() = lagMelding(AksjonType.START)

    fun lagTotalMelding() = lagMelding(AksjonType.DATA).apply {
        this.total = opprettTotalData()
        this.periode = opprettPeriodeData()
        this.grunnlag = opprettGrunnlagsData()
    }

    fun lagSluttmelding() = lagMelding(AksjonType.AVSL)

    fun lagAvstemmingsmeldinger(oppdragsliste: List<OppdragLager>): List<Avstemmingsdata> {
        if (oppdragsliste.isEmpty()) error("Kan ikke lage avstemminger med tom liste")

        return opprettAvstemmingsdataLister(oppdragsliste)
    }

    private fun lagMelding(aksjonType: AksjonType): Avstemmingsdata =
        Avstemmingsdata().apply {
            aksjon = opprettAksjonsdata(aksjonType)
        }

    private fun opprettAksjonsdata(aksjonType: AksjonType): Aksjonsdata {
        return Aksjonsdata().apply {
            this.aksjonType = aksjonType
            this.kildeType = KildeType.AVLEV
            this.avstemmingType = AvstemmingType.GRSN
            this.avleverendeKomponentKode = fagområdeTilAvleverendeKomponentKode(fagområde)
            this.mottakendeKomponentKode = SystemKode.OPPDRAGSSYSTEMET.kode
            this.underkomponentKode = fagområde
            this.nokkelFom = fom.format(tidspunktFormatter)
            this.nokkelTom = tom.format(tidspunktFormatter)
            this.avleverendeAvstemmingId = avstemmingId
            this.brukerId = fagområde
        }
    }

    private fun opprettAvstemmingsdataLister(oppdragsliste: List<OppdragLager>): List<Avstemmingsdata> {
        return opprettDetaljdata(oppdragsliste).chunked(ANTALL_DETALJER_PER_MELDING).map {
            lagMelding(AksjonType.DATA).apply {
                this.detalj.addAll(it)
            }
        }
    }

    private fun opprettDetaljdata(oppdragsliste: List<OppdragLager>): List<Detaljdata> {
        return oppdragsliste.mapNotNull { oppdrag ->

            leggTilGrunnlagsinformasjon(oppdrag)
            leggTilTotalData(oppdrag)
            håndterAvstemmingstidspunkt(oppdrag)

            val detaljType = opprettDetaljType(oppdrag)
            if (detaljType != null) {
                val utbetalingsoppdrag = oppdrag.utbetalingsoppdrag
                Detaljdata().apply {
                    this.detaljType = detaljType
                    this.offnr = utbetalingsoppdrag.aktoer
                    this.avleverendeTransaksjonNokkel = fagområde
                    this.tidspunkt = oppdrag.avstemmingTidspunkt.format(tidspunktFormatter)
                    if (detaljType in listOf(DetaljType.AVVI, DetaljType.VARS) && oppdrag.kvitteringsmelding != null) {
                        val kvitteringsmelding = oppdrag.kvitteringsmelding
                        this.meldingKode = kvitteringsmelding.kodeMelding
                        this.alvorlighetsgrad = kvitteringsmelding.alvorlighetsgrad
                        this.tekstMelding = kvitteringsmelding.beskrMelding
                    }
                }
            } else {
                null
            }
        }
    }

    private fun håndterAvstemmingstidspunkt(oppdrag: OppdragLager) {
        val fom = avstemmingstidspunkt.fom
        val tom = avstemmingstidspunkt.tom
        if (fom == null || fom > oppdrag.avstemmingTidspunkt) {
            avstemmingstidspunkt.fom = oppdrag.avstemmingTidspunkt
        }
        if (tom == null || tom > oppdrag.avstemmingTidspunkt) {
            avstemmingstidspunkt.tom = oppdrag.avstemmingTidspunkt
        }
    }

    private fun leggTilGrunnlagsinformasjon(oppdrag: OppdragLager) {
        val satsbeløp = getSatsBeløp(oppdrag)
        when (oppdrag.status) {
            OppdragStatus.LAGT_PÅ_KØ -> {
                grunnlagsdata.manglerBelop += satsbeløp
                grunnlagsdata.manglerAntall++
            }

            OppdragStatus.KVITTERT_OK -> {
                grunnlagsdata.godkjentBelop += satsbeløp
                grunnlagsdata.godkjentAntall++
            }

            OppdragStatus.KVITTERT_MED_MANGLER -> {
                grunnlagsdata.varselBelop += satsbeløp
                grunnlagsdata.varselAntall++
            }

            else -> {
                grunnlagsdata.avvistBelop += satsbeløp
                grunnlagsdata.avvistAntall++
            }
        }
    }

    private fun leggTilTotalData(oppdrag: OppdragLager) {
        total.antall++
        total.beløp += getSatsBeløp(oppdrag)
    }

    private fun opprettDetaljType(oppdrag: OppdragLager): DetaljType? =
        when (oppdrag.status) {
            OppdragStatus.LAGT_PÅ_KØ -> DetaljType.MANG
            OppdragStatus.KVITTERT_MED_MANGLER -> DetaljType.VARS
            OppdragStatus.KVITTERT_FUNKSJONELL_FEIL -> DetaljType.AVVI
            OppdragStatus.KVITTERT_TEKNISK_FEIL -> DetaljType.AVVI
            OppdragStatus.KVITTERT_OK -> null
            OppdragStatus.KVITTERT_UKJENT -> null
        }

    private fun opprettTotalData(): Totaldata {
        return Totaldata().apply {
            this.totalAntall = total.antall
            this.totalBelop = BigDecimal.valueOf(total.beløp)
            this.fortegn = getFortegn(total.beløp)
        }
    }

    private fun opprettPeriodeData(): Periodedata {
        val fom = avstemmingstidspunkt.fom
            ?: error("Mangler avstemmingstidspunkt::fom, vi skal ikke opprette meldinger hvis listen med oppdrag er tom")
        val tom = avstemmingstidspunkt.tom
            ?: error("Mangler avstemmingstidspunkt::tom, vi skal ikke opprette meldinger hvis listen med oppdrag er tom")
        return Periodedata().apply {
            this.datoAvstemtFom = formaterTilPeriodedataFormat(fom.format(tidspunktFormatter))
            this.datoAvstemtTom = formaterTilPeriodedataFormat(tom.format(tidspunktFormatter))
        }
    }

    private fun opprettGrunnlagsData(): Grunnlagsdata {
        return Grunnlagsdata().apply {
            this.godkjentAntall = grunnlagsdata.godkjentAntall
            this.godkjentBelop = BigDecimal.valueOf(grunnlagsdata.godkjentBelop)
            this.godkjentFortegn = getFortegn(grunnlagsdata.godkjentBelop)

            this.varselAntall = varselAntall
            this.varselBelop = BigDecimal.valueOf(grunnlagsdata.varselBelop)
            this.varselFortegn = getFortegn(grunnlagsdata.varselBelop)

            this.manglerAntall = manglerAntall
            this.manglerBelop = BigDecimal.valueOf(grunnlagsdata.manglerBelop)
            this.manglerFortegn = getFortegn(grunnlagsdata.manglerBelop)

            this.avvistAntall = avvistAntall
            this.avvistBelop = BigDecimal.valueOf(grunnlagsdata.avvistBelop)
            this.avvistFortegn = getFortegn(grunnlagsdata.avvistBelop)
        }
    }

    private fun getSatsBeløp(oppdrag: OppdragLager): Long =
        oppdrag.utbetalingsoppdrag.utbetalingsperiode.map { it.sats }.reduce(BigDecimal::add).toLong()

    private fun getFortegn(satsbeløp: Long): Fortegn {
        return if (satsbeløp >= 0) Fortegn.T else Fortegn.F
    }

    private fun formaterTilPeriodedataFormat(stringTimestamp: String): String =
        LocalDateTime.parse(stringTimestamp, tidspunktFormatter)
            .format(DateTimeFormatter.ofPattern("yyyyMMddHH"))
}

private data class Grunnlag(
    var godkjentAntall: Int = 0,
    var godkjentBelop: Long = 0L,
    var varselAntall: Int = 0,
    var varselBelop: Long = 0L,
    var avvistAntall: Int = 0,
    var avvistBelop: Long = 0L,
    var manglerAntall: Int = 0,
    var manglerBelop: Long = 0L,
)

data class Total(
    var antall: Int = 0,
    var beløp: Long = 0L,
)

data class Avstemmingstidspunkt(
    var fom: LocalDateTime? = null,
    var tom: LocalDateTime? = null,
)
