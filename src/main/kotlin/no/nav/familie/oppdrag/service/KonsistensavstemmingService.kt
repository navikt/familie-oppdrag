package no.nav.familie.oppdrag.service

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.konsistensavstemming.KonsistensavstemmingMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class KonsistensavstemmingService(private val avstemmingSender: AvstemmingSender) {

    fun utførKonsistensavstemming(fagsystem: String, utbetalingsoppdrag: List<Utbetalingsoppdrag>, avstemmingsdato: LocalDateTime) {
        val konsistensavstemmingMapper = KonsistensavstemmingMapper(fagsystem, utbetalingsoppdrag, avstemmingsdato)
        val meldinger = konsistensavstemmingMapper.lagAvstemmingsmeldinger()

        if (meldinger.isEmpty()) {
            LOG.info("Ingen oppdrag å utføre konsistensavstemming for")
            return
        }

        LOG.info("Utfører konsistensavstemming for id ${konsistensavstemmingMapper.avstemmingId}, antall meldinger er ${meldinger.size}")
        meldinger.forEach {
            avstemmingSender.sendKonsistensAvstemming(it)
        }

        LOG.info("Fullført konsistensavstemming for id ${konsistensavstemmingMapper.avstemmingId}")
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(KonsistensavstemmingService::class.java)
    }
}