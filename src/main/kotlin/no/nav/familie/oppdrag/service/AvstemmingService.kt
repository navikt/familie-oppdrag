package no.nav.familie.oppdrag.service

import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Avstemmingsdata
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AvstemmingService(
        @Autowired private val avstemmingSender: AvstemmingSender,
        @Autowired private val oppdragProtokollRepository: OppdragProtokollRepository) {

    fun utførGrensesnittavstemming(listeAvstemmingMeldinger: List<Avstemmingsdata>) {

        LOG.debug("Utfører grensesnittavstemming for ${listeAvstemmingMeldinger.size} antall meldinger.")

        listeAvstemmingMeldinger.forEach {
            avstemmingSender.sendGrensesnittAvstemming(it)
        }

        // lagre i basen?
        // oppdatere status til avstemt?
    }

    companion object {
        val LOG = LoggerFactory.getLogger(AvstemmingService::class.java)
    }

}