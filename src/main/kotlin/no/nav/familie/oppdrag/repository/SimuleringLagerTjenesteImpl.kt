package no.nav.familie.oppdrag.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class SimuleringLagerTjenesteImpl : SimuleringLagerTjeneste {
    @Autowired lateinit var simuleringLagerRepository: SimuleringLagerRepository

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun lagreINyTransaksjon(simuleringLager: SimuleringLager) {
        simuleringLagerRepository.insert(simuleringLager)
    }

    override fun oppdater(simuleringLager: SimuleringLager) {
        simuleringLagerRepository.update(simuleringLager)
    }

    override fun finnAlleSimuleringsLager(): List<SimuleringLager> = simuleringLagerRepository.findAll().toList()

    override fun hentSisteSimuleringsresultat(
        fagsystem: String,
        fagsakId: String,
        behandlingId: String,
    ): SimuleringLager = simuleringLagerRepository.finnSisteSimuleringsresultat(fagsystem, fagsakId, behandlingId)
}
