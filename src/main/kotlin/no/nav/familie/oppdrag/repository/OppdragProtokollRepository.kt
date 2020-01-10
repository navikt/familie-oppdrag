package no.nav.familie.oppdrag.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface OppdragProtokollRepository : CrudRepository<OppdragProtokoll, Long> {

    @Query("SELECT * FROM OPPDRAG_PROTOKOLL WHERE behandling_id = :behandlingId AND person_ident = :personIdent AND fagsystem = :fagsystem")
    fun hentEksisterendeOppdrag(fagsystem: String, behandlingId: String, personIdent: String): List<OppdragProtokoll>
}