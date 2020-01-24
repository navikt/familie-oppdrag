package no.nav.familie.oppdrag.service

import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.grensesnittavstemming.AvstemmingMapper
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
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
        val avstemmingMapper = AvstemmingMapper(oppdragSomSkalAvstemmes, fagsystem)
        val meldinger = avstemmingMapper.lagAvstemmingsmeldinger()

        LOG.info("Utfører grensesnittavstemming for id: ${avstemmingMapper.avstemmingId}, ${meldinger.size} antall meldinger.")

        meldinger.forEach {
            avstemmingSender.sendGrensesnittAvstemming(it)
        }

        LOG.info("Fullført grensesnittavstemming for id: ${avstemmingMapper.avstemmingId}")
    }

    companion object {
        val LOG = LoggerFactory.getLogger(AvstemmingService::class.java)
    }

}