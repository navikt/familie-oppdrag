package no.nav.familie.oppdrag.service

import io.micrometer.core.instrument.Metrics;
import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.grensesnittavstemming.AvstemmingMapper
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Grunnlagsdata
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AvstemmingService(
        @Autowired private val avstemmingSender: AvstemmingSender,
        @Autowired private val oppdragLagerRepository: OppdragLagerRepository) {

    fun utførGrensesnittavstemming(fagsystem: String, fom: LocalDateTime, tom: LocalDateTime) {
        val oppdragSomSkalAvstemmes = oppdragLagerRepository.hentIverksettingerForGrensesnittavstemming(fom, tom, fagsystem)
        val avstemmingMapper = AvstemmingMapper(oppdragSomSkalAvstemmes, fagsystem, fom, tom)
        val meldinger = avstemmingMapper.lagAvstemmingsmeldinger()

        if (meldinger.isEmpty()) {
            LOG.info("Ingen oppdrag å gjennomføre grensesnittavstemming for.")
            return
        }

        LOG.info("Utfører grensesnittavstemming for id: ${avstemmingMapper.avstemmingId}, ${meldinger.size} antall meldinger.")

        meldinger.forEach {
            avstemmingSender.sendGrensesnittAvstemming(it)
        }

        LOG.info("Fullført grensesnittavstemming for id: ${avstemmingMapper.avstemmingId}")

        oppdaterMetrikker(fagsystem, meldinger[1].grunnlag)
    }

    fun oppdaterMetrikker(fagsystem: String, grunnlag: Grunnlagsdata) {
        Metrics.counter("familie.oppdrag.grensesnittavstemming","fagsystem", fagsystem, "status", Status.GODKJENT.status,
                "beskrivelse", Status.GODKJENT.beskrivelse).increment(grunnlag.godkjentAntall.toDouble())
        Metrics.counter("familie.oppdrag.grensesnittavstemming","fagsystem", fagsystem, "status", Status.AVVIST.status,
                "beskrivelse", Status.AVVIST.beskrivelse).increment(grunnlag.avvistAntall.toDouble())
        Metrics.counter("familie.oppdrag.grensesnittavstemming","fagsystem", fagsystem, "status", Status.MANGLER.status,
                "beskrivelse", Status.MANGLER.beskrivelse).increment(grunnlag.manglerAntall.toDouble())
        Metrics.counter("familie.oppdrag.grensesnittavstemming","fagsystem", fagsystem, "status", Status.VARSEL.status,
                "beskrivelse", Status.VARSEL.beskrivelse).increment(grunnlag.varselAntall.toDouble())
    }

    companion object {
        val LOG = LoggerFactory.getLogger(AvstemmingService::class.java)
    }

}

enum class Status(val status : String, val beskrivelse : String) {
    GODKJENT("godkjent", "Antall oppdrag som har fått OK kvittering (alvorlighetsgrad 00)."),
    AVVIST("avvist", "Antall oppdrag som har fått kvittering med funksjonell eller teknisk feil, samt ukjent (alvorlighetsgrad 08 og 12)."),
    MANGLER("mangler", "Antall oppdrag hvor kvittering mangler."),
    VARSEL("varsel", "Antall oppdrag som har fått kvittering med mangler (alvorlighetsgrad 04).")
}