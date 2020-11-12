package no.nav.familie.oppdrag.grensesnittavstemming

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.avstemming.AvstemmingMapper
import no.nav.familie.oppdrag.avstemming.AvstemmingMapper.fagområdeTilAvleverendeKomponentKode
import no.nav.familie.oppdrag.avstemming.SystemKode
import no.nav.familie.oppdrag.repository.OppdragLager
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.*
import no.trygdeetaten.skjema.oppdrag.Mmel
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class GrensesnittavstemmingMapper(private val oppdragsliste: List<OppdragLager>,
                                  private val fagområde: String,
                                  private val fom: LocalDateTime,
                                  private val tom: LocalDateTime) {
    private val ANTALL_DETALJER_PER_MELDING = 70
    private val tidspunktFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")
    val avstemmingId = AvstemmingMapper.encodeUUIDBase64(UUID.randomUUID())

    fun lagAvstemmingsmeldinger() : List<Avstemmingsdata> {
        if (oppdragsliste.isEmpty())
            return emptyList()
        else
            return (listOf(lagStartmelding()) + lagDatameldinger() + listOf(lagSluttmelding()))
    }

    private fun lagStartmelding() = lagMelding(AksjonType.START)

    private fun lagSluttmelding() = lagMelding(AksjonType.AVSL)

    private fun lagDatameldinger(): List<Avstemmingsdata> {
        val detaljMeldinger = opprettAvstemmingsdataLister()

        val avstemmingsDataLister = if (detaljMeldinger.isNotEmpty()) detaljMeldinger else listOf(lagMelding(AksjonType.DATA))
        avstemmingsDataLister.first().apply {
            this.total = opprettTotalData()
            this.periode = opprettPeriodeData()
            this.grunnlag = opprettGrunnlagsData()
        }

        return avstemmingsDataLister
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

    private fun opprettAvstemmingsdataLister() : List<Avstemmingsdata> {
        return opprettDetaljdata().chunked(ANTALL_DETALJER_PER_MELDING).map {
            lagMelding(AksjonType.DATA).apply {
                this.detalj.addAll(it)
            }
        }
    }

    private fun opprettDetaljdata() : List<Detaljdata> {
        return oppdragsliste.mapNotNull { oppdrag ->
            val detaljType = opprettDetaljType(oppdrag)
            if (detaljType != null) {
                val utbetalingsoppdrag = fraInputDataTilUtbetalingsoppdrag(oppdrag.utbetalingsoppdrag)
                Detaljdata().apply {
                    this.detaljType = detaljType
                    this.offnr = utbetalingsoppdrag.aktoer
                    this.avleverendeTransaksjonNokkel = fagområde
                    this.tidspunkt = oppdrag.avstemmingTidspunkt.format(tidspunktFormatter)
                    if (detaljType in listOf(DetaljType.AVVI, DetaljType.VARS) && oppdrag.kvitteringsmelding != null) {
                        val kvitteringsmelding = fraKvitteringTilKvitteringsmelding(oppdrag.kvitteringsmelding)
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

    private fun opprettDetaljType(oppdrag : OppdragLager) : DetaljType? =
            when (oppdrag.status) {
                OppdragStatus.LAGT_PÅ_KØ -> DetaljType.MANG
                OppdragStatus.KVITTERT_MED_MANGLER -> DetaljType.VARS
                OppdragStatus.KVITTERT_FUNKSJONELL_FEIL -> DetaljType.AVVI
                OppdragStatus.KVITTERT_TEKNISK_FEIL -> DetaljType.AVVI
                OppdragStatus.KVITTERT_OK -> null
                OppdragStatus.KVITTERT_UKJENT -> null
            }

    private fun fraInputDataTilUtbetalingsoppdrag(inputData : String) : Utbetalingsoppdrag =
        objectMapper.readValue(inputData)

    private fun fraKvitteringTilKvitteringsmelding(kvittering : String) : Mmel =
            objectMapper.readValue(kvittering)

    private fun opprettTotalData() : Totaldata {
        val totalBeløp = oppdragsliste.map { getSatsBeløp(it) }.sum()
        return Totaldata().apply {
            this.totalAntall = oppdragsliste.size
            this.totalBelop = BigDecimal.valueOf(totalBeløp)
            this.fortegn = getFortegn(totalBeløp)
        }
    }

    private fun opprettPeriodeData(): Periodedata {
        return Periodedata().apply {
            this.datoAvstemtFom = formaterTilPeriodedataFormat(getLavesteAvstemmingstidspunkt().format(tidspunktFormatter))
            this.datoAvstemtTom = formaterTilPeriodedataFormat(getHøyesteAvstemmingstidspunkt().format(tidspunktFormatter))
        }
    }

    private fun opprettGrunnlagsData(): Grunnlagsdata {
        var godkjentAntall = 0
        var godkjentBelop = 0L
        var varselAntall = 0
        var varselBelop = 0L
        var avvistAntall = 0
        var avvistBelop = 0L
        var manglerAntall = 0
        var manglerBelop = 0L

        for (oppdrag in oppdragsliste) {
            val satsbeløp = getSatsBeløp(oppdrag)
            when (oppdrag.status) {
                OppdragStatus.LAGT_PÅ_KØ -> {
                    manglerBelop += satsbeløp
                    manglerAntall++ }
                OppdragStatus.KVITTERT_OK -> {
                    godkjentBelop += satsbeløp
                    godkjentAntall++ }
                OppdragStatus.KVITTERT_MED_MANGLER -> {
                    varselBelop += satsbeløp
                    varselAntall++ }
                else -> {
                    avvistBelop += satsbeløp
                    avvistAntall++ }
            }
        }

        return Grunnlagsdata().apply {
            this.godkjentAntall = godkjentAntall
            this.godkjentBelop = BigDecimal.valueOf(godkjentBelop)
            this.godkjentFortegn = getFortegn(godkjentBelop)

            this.varselAntall = varselAntall
            this.varselBelop = BigDecimal.valueOf(varselBelop)
            this.varselFortegn = getFortegn(varselBelop)

            this.manglerAntall = manglerAntall
            this.manglerBelop = BigDecimal.valueOf(manglerBelop)
            this.manglerFortegn = getFortegn(manglerBelop)

            this.avvistAntall = avvistAntall
            this.avvistBelop = BigDecimal.valueOf(avvistBelop)
            this.avvistFortegn = getFortegn(avvistBelop)
        }
    }

    private fun getSatsBeløp(oppdrag: OppdragLager) : Long =
            fraInputDataTilUtbetalingsoppdrag(oppdrag.utbetalingsoppdrag).utbetalingsperiode.map { it.sats }.reduce(BigDecimal::add).toLong()

    private fun getFortegn(satsbeløp: Long): Fortegn {
        return if (satsbeløp >= 0) Fortegn.T else Fortegn.F
    }

    private fun getHøyesteAvstemmingstidspunkt(): LocalDateTime {
        return sortertAvstemmingstidspunkt().first()
    }

    private fun getLavesteAvstemmingstidspunkt(): LocalDateTime {
        return sortertAvstemmingstidspunkt().last()
    }

    private fun sortertAvstemmingstidspunkt() =
            oppdragsliste.map(OppdragLager::avstemmingTidspunkt).sortedDescending()

    private fun formaterTilPeriodedataFormat(stringTimestamp: String): String =
            LocalDateTime.parse(stringTimestamp, tidspunktFormatter)
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHH"))

}