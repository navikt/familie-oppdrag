package no.nav.familie.oppdrag.service

import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.grensesnittavstemming.AvstemmingMapper
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AvstemmingService(
        @Autowired private val avstemmingSender: AvstemmingSender,
        @Autowired private val oppdragProtokollRepository: OppdragProtokollRepository) {

    fun utførGrensesnittavstemming(fagsystem: String, fom: LocalDateTime, tom: LocalDateTime) {
        val oppdragSomSkalAvstemmes = oppdragProtokollRepository.hentIverksettingerForGrensesnittavstemming(fom, tom, fagsystem)

        val meldinger = AvstemmingMapper(oppdragSomSkalAvstemmes, fagsystem).lagAvstemmingsmeldinger()

        LOG.debug("Utfører grensesnittavstemming for ${meldinger.size} antall meldinger.")

        meldinger.forEach {
            avstemmingSender.sendGrensesnittAvstemming(it)
        }

        // lagre i basen?
        // oppdatere status til avstemt?
    }

    companion object {
        val LOG = LoggerFactory.getLogger(AvstemmingService::class.java)
    }

}