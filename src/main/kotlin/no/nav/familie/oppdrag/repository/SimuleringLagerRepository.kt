package no.nav.familie.oppdrag.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SimuleringLagerRepository : RepositoryInterface<SimuleringLager, UUID>, InsertUpdateRepository<SimuleringLager> {
    // language=PostgreSQL
    @Query(
        """
            SELECT sim.* FROM simulering_lager sim 
             WHERE sim.fagsystem=:fagsystem AND sim.fagsak_id=:fagsakId AND sim.behandling_id=:behandlingId
             AND sim.response_xml IS NOT NULL
             ORDER BY sim.opprettet_tidspunkt DESC LIMIT 1
    """,
    )
    fun finnSisteSimuleringsresultat(
        fagsystem: String,
        fagsakId: String,
        behandlingId: String,
    ): SimuleringLager
}
