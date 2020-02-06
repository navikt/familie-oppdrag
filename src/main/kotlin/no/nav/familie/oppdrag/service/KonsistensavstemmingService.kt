package no.nav.familie.oppdrag.service

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.konsistensavstemming.KonsistensavstemmingMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class KonsistensavstemmingService(
        private val avstemmingSender: AvstemmingSender) {

    fun utførKonsistensavstemming(fagsystem: String, utbetalingsoppdrag: List<Utbetalingsoppdrag>, avstemmingsdato: LocalDateTime) {
        val konsistensavstemmingMapper = KonsistensavstemmingMapper(fagsystem, utbetalingsoppdrag, avstemmingsdato)
        konsistensavstemmingMapper.lagAvstemmingsmeldinger()
    }
}