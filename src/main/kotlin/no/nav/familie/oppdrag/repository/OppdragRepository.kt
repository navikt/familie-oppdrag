package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import org.springframework.data.jdbc.repository.query.Query
import java.time.LocalDateTime
import java.util.*

// language=PostgreSQL
interface OppdragRepository : RepositoryInterface<OppdragLager, UUID>, InsertUpdateRepository<OppdragLager> {

    @Query("""SELECT * FROM oppdrag_lager 
                     WHERE fagsystem = :fagsystem 
                     AND person_ident = :personIdent 
                     AND behandling_id = :behandlingId 
                     AND versjon = :versjon""")
    fun hentOppdrag(fagsystem: String,
                    personIdent: String,
                    behandlingId: String,
                    versjon: Int = 0): OppdragLager

    @Query("""SELECT utbetalingsoppdrag FROM oppdrag_lager 
                     WHERE fagsystem = :fagsystem 
                     AND person_ident = :personIdent 
                     AND behandling_id = :behandlingId 
                     AND versjon = :versjon""")
    fun hentUtbetalingsoppdrag(fagsystem: String,
                               personIdent: String,
                               behandlingId: String,
                               versjon: Int = 0): Utbetalingsoppdrag

    @Query("""SELECT * FROM oppdrag_lager 
                     WHERE fagsystem = :fagsystem
                     AND person_ident = :personIdent
                     AND behandling_id = :behandlingId""")
    fun hentAlleVersjonerAvOppdrag(fagsystem: String,
                                   personIdent: String,
                                   behandlingId: String): List<OppdragLager>

    @Query("""SELECT * FROM oppdrag_lager 
                     WHERE avstemming_tidspunkt >= :fomTidspunkt 
                     AND avstemming_tidspunkt < :tomTidspunkt 
                     AND fagsystem = :fagområde""")
    fun hentIverksettingerForGrensesnittavstemming(fomTidspunkt: LocalDateTime,
                                                   tomTidspunkt: LocalDateTime,
                                                   fagområde: String): List<OppdragLager>

    @Query("""SELECT fagsak_id, behandling_id, utbetalingsoppdrag 
                     FROM (SELECT fagsak_id, behandling_id, utbetalingsoppdrag, 
                           row_number() OVER (PARTITION BY fagsak_id, behandling_id ORDER BY versjon DESC) rn
                           FROM oppdrag_lager 
                           WHERE fagsystem=:fagsystem 
                           AND behandling_id IN (:behandlingIder)
                           AND status IN (:statuser)) q 
                      WHERE rn = 1""")
    fun hentUtbetalingsoppdragForKonsistensavstemming(fagsystem: String,
                                                      behandlingIder: Set<String>,
                                                      statuser: Set<String> = setOf(OppdragStatus.KVITTERT_OK.name,
                                                                                    OppdragStatus.KVITTERT_MED_MANGLER.name))
            : List<UtbetalingsoppdragForKonsistensavstemming>
}
