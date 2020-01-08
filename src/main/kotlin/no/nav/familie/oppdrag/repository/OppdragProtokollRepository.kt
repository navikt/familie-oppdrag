package no.nav.familie.oppdrag.repository

import no.nav.familie.oppdrag.domene.OppdragId
import org.springframework.data.repository.CrudRepository

interface OppdragProtokollRepository : CrudRepository<OppdragProtokoll, OppdragId> {
}