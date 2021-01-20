package no.nav.familie.oppdrag.repository

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import java.util.*


interface OppdragRepository : RepositoryInterface<OppdragLager, UUID>, InsertUpdateRepository<OppdragLager> {

    @Query("""select * from oppdrag_lager where id is null limit 10000""")
    fun findWhereUuidIsNull(): Iterable<OppdragLager>


    @Modifying
    @Query("""update oppdrag_lager set id = :uuid 
             WHERE behandling_id = :behandlingsId 
             AND person_ident = :personIdent 
             AND fagsystem = :fagsystem 
             AND versjon = :versjon""")
    fun updateUuid(behandlingsId: String,
                   personIdent: String,
                   fagsystem: String,
                   versjon: Int,
                   uuid: UUID = UUID.randomUUID())


}