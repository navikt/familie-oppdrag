package no.nav.familie.oppdrag.service

import no.nav.familie.oppdrag.repository.OppdragRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DbMigreringService(private val oppdragRepository: OppdragRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 120000, fixedDelay = 3600000)
    @Transactional
    fun dbMigrering() {

        val iterable = oppdragRepository.findWhereUuidIsNull()

        if (iterable.none()) {
            logger.info("Migrering for uuid fullført.")
            return
        }


        iterable.forEach { oppdragRepository.updateUuid(it.behandlingId, it.personIdent, it.fagsystem, it.versjon) }
        logger.info("Migrert  ${iterable.count()} oppdraglager for uuid.")

    }

}
