package no.nav.familie.oppdrag.grensesnittavstemming

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.iverksetting.Jaxb
import no.nav.familie.oppdrag.repository.OppdragProtokoll
import no.nav.familie.oppdrag.repository.OppdragProtokollStatus
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.*
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AvstemmingMapper(private val oppdragsliste: List<OppdragProtokoll>,
                       private val fagOmråde: String,
                       private val jaxb: Jaxb = Jaxb()) {
    val objectFactory = ObjectFactory()
    val ANTALL_DETALJER_PER_MELDING = 70
    val tidspunktFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")

    fun lagAvstemmingsmeldinger() : List<Avstemmingsdata> {
        if (oppdragsliste.isEmpty())
            return emptyList()
        else
            return (listOf(lagStartmelding()) + lagDatameldinger() + listOf(lagSluttmelding()))
    }

    fun lagStartmelding() = lagMelding(AksjonType.START)

    fun lagSluttmelding() = lagMelding(AksjonType.AVSL)

    fun lagDatameldinger(): List<Avstemmingsdata> {
        val detaljMeldinger = opprettAvstemmingsdataLister()

        val avstemmingsDataLister = if (detaljMeldinger.isNotEmpty()) detaljMeldinger else listOf(lagMelding(AksjonType.DATA))
        avstemmingsDataLister.first().apply {
            this.total = opprettTotalData()
            this.periode = opprettPeriodeData()
            this.grunnlag = opprettGrunnlagsData()
        }

        return avstemmingsDataLister
    }

    fun lagMelding(aksjonType: AksjonType): Avstemmingsdata =
        objectFactory.createAvstemmingsdata().apply {
            aksjon = opprettAksjonsdata(aksjonType)
        }

    fun opprettAksjonsdata(aksjonType: AksjonType): Aksjonsdata {
        return objectFactory.createAksjonsdata().apply {
            this.aksjonType = aksjonType
            this.kildeType = KildeType.AVLEV
            this.avstemmingType = AvstemmingType.GRSN
            this.avleverendeKomponentKode = fagOmråde
            this.mottakendeKomponentKode = SystemKode.OPPDRAGSSYSTEMET.kode
            this.underkomponentKode = fagOmråde
            this.nokkelFom = getLavesteAvstemmingstidspunkt().toString()
            this.nokkelTom = getHøyesteAvstemmingstidspunkt().toString()
            this.tidspunktAvstemmingTom = LocalDateTime.now().toString() // TODO TRENGER VI DENNE?
            this.avleverendeAvstemmingId = encodeUUIDBase64(UUID.randomUUID())
            this.brukerId = fagOmråde
        }
    }

    fun encodeUUIDBase64(uuid: UUID): String {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return Base64.getUrlEncoder().encodeToString(bb.array()).substring(0, 22)
    }
    /*
        private static String encodeUUIDBase64(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().encodeToString(bb.array()).substring(0, 22);
    }
     */

    fun opprettAvstemmingsdataLister() : List<Avstemmingsdata> {
        return opprettDetaljdata().chunked(ANTALL_DETALJER_PER_MELDING).map {
            lagMelding(AksjonType.DATA).apply {
                this.detalj.addAll(it)
            }
        }
    }

    fun opprettDetaljdata() : List<Detaljdata> {
        return oppdragsliste.mapNotNull { oppdrag ->
            val detaljType = opprettDetaljType(oppdrag)
            if (detaljType != null) {
                val utbetalingsoppdrag = fraInputDataTilUtbetalingsoppdrag(oppdrag.inputData)
                objectFactory.createDetaljdata().apply {
                    this.detaljType = detaljType
                    this.offnr = utbetalingsoppdrag.aktoer
                    this.avleverendeTransaksjonNokkel = fagOmråde
                    this.tidspunkt = oppdrag.avstemmingTidspunkt.toString()
                    if (detaljType in listOf(DetaljType.AVVI, DetaljType.VARS)) {
                        val kvitteringsmelding = fraMeldingTilOppdrag(oppdrag.melding)
                        this.meldingKode = kvitteringsmelding.mmel.kodeMelding
                        this.alvorlighetsgrad = kvitteringsmelding.mmel.alvorlighetsgrad
                        this.tekstMelding = kvitteringsmelding.mmel.beskrMelding
                    }
                }
            } else {
                null
            }
        }
    }

    fun opprettDetaljType(oppdrag : OppdragProtokoll) : DetaljType? =
            when (oppdrag.status) {
                OppdragProtokollStatus.LAGT_PÅ_KØ -> DetaljType.MANG
                OppdragProtokollStatus.KVITTERT_MED_MANGLER -> DetaljType.VARS
                OppdragProtokollStatus.KVITTERT_FUNKSJONELL_FEIL -> DetaljType.AVVI
                OppdragProtokollStatus.KVITTERT_TEKNISK_FEIL -> DetaljType.AVVI
                OppdragProtokollStatus.KVITTERT_OK -> null
                OppdragProtokollStatus.KVITTERT_UKJENT -> null
            }

    fun fraInputDataTilUtbetalingsoppdrag(inputData : String) : Utbetalingsoppdrag =
        objectMapper.readValue(inputData)

    fun fraMeldingTilOppdrag(melding : String) : Oppdrag =
            jaxb.tilOppdrag(melding)

    fun opprettTotalData() : Totaldata {
        val totalBeløp = oppdragsliste.map { getSatsBeløp(it) }.sum()
        return objectFactory.createTotaldata().apply {
            this.totalAntall = oppdragsliste.size
            this.totalBelop = BigDecimal.valueOf(totalBeløp)
            this.fortegn = getFortegn(totalBeløp)
        }
    }

    fun opprettPeriodeData(): Periodedata {
        return objectFactory.createPeriodedata().apply {
            this.datoAvstemtFom = formaterTilPeriodedataFormat(getLavesteAvstemmingstidspunkt().toString())
            this.datoAvstemtTom = formaterTilPeriodedataFormat(getHøyesteAvstemmingstidspunkt().toString())
        }
    }

    fun opprettGrunnlagsData(): Grunnlagsdata {
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
            if (OppdragProtokollStatus.LAGT_PÅ_KØ == oppdrag.status) {
                manglerBelop += satsbeløp
                manglerAntall++
            } else if (OppdragProtokollStatus.KVITTERT_OK == oppdrag.status) {
                godkjentBelop += satsbeløp
                godkjentAntall++
            } else if (OppdragProtokollStatus.KVITTERT_MED_MANGLER == oppdrag.status) {
                varselBelop += satsbeløp
                varselAntall++
            } else {
                avvistBelop += satsbeløp
                avvistAntall++
            }
        }

        return objectFactory.createGrunnlagsdata().apply {
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

    fun getSatsBeløp(oppdrag: OppdragProtokoll) : Long =
            fraInputDataTilUtbetalingsoppdrag(oppdrag.inputData).utbetalingsperiode.map { it.sats }.reduce(BigDecimal::add).toLong()

    fun getFortegn(satsbeløp: Long): Fortegn {
        return if (satsbeløp >= 0) Fortegn.T else Fortegn.F
    }

    fun getHøyesteAvstemmingstidspunkt(): LocalDateTime {
        return sortertAvstemmingstidspunkt().first()
    }

    fun getLavesteAvstemmingstidspunkt(): LocalDateTime {
        return sortertAvstemmingstidspunkt().last()
    }

    fun sortertAvstemmingstidspunkt() =
            oppdragsliste.map(OppdragProtokoll::avstemmingTidspunkt).sortedDescending()

    fun formaterTilPeriodedataFormat(stringTimestamp: String): String =
            LocalDateTime.parse(stringTimestamp, tidspunktFormatter)
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHH"))

}

enum class SystemKode(val kode : String) {
    OPPDRAGSSYSTEMET("OS")
    // legge til for de andre systemene vi skal kommunisere med?
}