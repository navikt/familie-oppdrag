package no.nav.familie.oppdrag.repository

import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SimuleringLagerRepository: RepositoryInterface<SimuleringLager, UUID>, InsertUpdateRepository<SimuleringLager> {
}
