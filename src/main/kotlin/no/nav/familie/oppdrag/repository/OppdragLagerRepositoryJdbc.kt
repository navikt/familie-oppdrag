package no.nav.familie.oppdrag.repository

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.trygdeetaten.skjema.oppdrag.Mmel
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.UUID

@Repository
class OppdragLagerRepositoryJdbc(
    val jdbcTemplate: JdbcTemplate,
    val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) : OppdragLagerRepository {

    internal var LOG = LoggerFactory.getLogger(OppdragLagerRepositoryJdbc::class.java)

    override fun hentOppdrag(oppdragId: OppdragId, versjon: Int): OppdragLager {
        val hentStatement =
            "SELECT * FROM oppdrag_lager WHERE behandling_id = ? AND person_ident = ? AND fagsystem = ? AND versjon = ?"

        val listeAvOppdrag = jdbcTemplate.query(
            hentStatement,
            arrayOf(
                oppdragId.behandlingsId,
                oppdragId.personIdent,
                oppdragId.fagsystem,
                versjon,
            ),
            OppdragLagerRowMapper(),
        )

        return when (listeAvOppdrag.size) {
            0 -> {
                LOG.error("Feil ved henting av oppdrag. Fant ingen oppdrag med id $oppdragId")
                throw NoSuchElementException("Feil ved henting av oppdrag. Fant ingen oppdrag med id $oppdragId")
            }

            1 -> listeAvOppdrag[0]
            else -> {
                LOG.error("Feil ved henting av oppdrag. Fant fler oppdrag med id $oppdragId")
                throw Exception("Feil ved henting av oppdrag. Fant fler oppdrag med id $oppdragId")
            }
        }
    }

    override fun opprettOppdrag(oppdragLager: OppdragLager, versjon: Int) {
        val insertStatement = "INSERT INTO oppdrag_lager " +
            "(id, utgaaende_oppdrag, status, opprettet_tidspunkt, person_ident, fagsak_id, behandling_id, fagsystem, avstemming_tidspunkt, utbetalingsoppdrag, versjon)" +
            " VALUES (?,?,?,?,?,?,?,?,?,?,?)"

        jdbcTemplate.update(
            insertStatement,
            UUID.randomUUID(),
            oppdragLager.utgåendeOppdrag,
            oppdragLager.status.name,
            oppdragLager.opprettetTidspunkt,
            oppdragLager.personIdent,
            oppdragLager.fagsakId,
            oppdragLager.behandlingId,
            oppdragLager.fagsystem,
            oppdragLager.avstemmingTidspunkt,
            objectMapper.writeValueAsString(oppdragLager.utbetalingsoppdrag),
            versjon,
        )
    }

    override fun oppdaterStatus(oppdragId: OppdragId, oppdragStatus: OppdragStatus, versjon: Int) {
        val update = "UPDATE oppdrag_lager SET status = '${oppdragStatus.name}' " +
            "WHERE person_ident = '${oppdragId.personIdent}' " +
            "AND fagsystem = '${oppdragId.fagsystem}' " +
            "AND behandling_id = '${oppdragId.behandlingsId}'" +
            "AND versjon = $versjon"

        jdbcTemplate.execute(update)
    }

    override fun oppdaterKvitteringsmelding(oppdragId: OppdragId, kvittering: Mmel, versjon: Int) {
        val updateStatement =
            "UPDATE oppdrag_lager SET kvitteringsmelding = ? WHERE person_ident = ? AND fagsystem = ? AND behandling_id = ? AND versjon = ?"

        jdbcTemplate.update(
            updateStatement,
            objectMapper.writeValueAsString(kvittering),
            oppdragId.personIdent,
            oppdragId.fagsystem,
            oppdragId.behandlingsId,
            versjon,
        )
    }

    override fun hentIverksettingerForGrensesnittavstemming(
        fomTidspunkt: LocalDateTime,
        tomTidspunkt: LocalDateTime,
        fagOmråde: String,
        antall: Int,
        page: Int,
    ): List<OppdragTilAvstemming> {
        val hentStatement = """
            SELECT 
            status, opprettet_tidspunkt, person_ident, fagsak_id, behandling_id, fagsystem, avstemming_tidspunkt, utbetalingsoppdrag, kvitteringsmelding
            FROM oppdrag_lager WHERE avstemming_tidspunkt >= ? AND avstemming_tidspunkt < ? AND fagsystem = ? 
            ORDER BY behandling_id ASC OFFSET ? LIMIT ?
            """

        return jdbcTemplate.query(
            hentStatement,
            OppdragTilAvstemmingRowMapper,
            *arrayOf(fomTidspunkt, tomTidspunkt, fagOmråde, page * antall, antall),
        )
    }

    override fun hentUtbetalingsoppdrag(oppdragId: OppdragId, versjon: Int): Utbetalingsoppdrag {
        val hentStatement =
            "SELECT utbetalingsoppdrag FROM oppdrag_lager WHERE behandling_id = ? AND person_ident = ? AND fagsystem = ? AND versjon = ?"

        val jsonUtbetalingsoppdrag = jdbcTemplate.queryForObject(
            hentStatement,
            arrayOf(oppdragId.behandlingsId, oppdragId.personIdent, oppdragId.fagsystem, versjon),
            String::class.java,
        )

        return objectMapper.readValue(jsonUtbetalingsoppdrag)
    }

    override fun hentAlleVersjonerAvOppdrag(oppdragId: OppdragId): List<OppdragLager> {
        val hentStatement = "SELECT * FROM oppdrag_lager WHERE behandling_id = ? AND person_ident = ? AND fagsystem = ?"

        return jdbcTemplate.query(
            hentStatement,
            arrayOf(oppdragId.behandlingsId, oppdragId.personIdent, oppdragId.fagsystem),
            OppdragLagerRowMapper(),
        )
    }

    override fun hentUtbetalingsoppdragForKonsistensavstemming(
        fagsystem: String,
        behandlingIder: Set<String>,
    ): List<UtbetalingsoppdragForKonsistensavstemming> {
        val query = """SELECT fagsak_id, behandling_id, utbetalingsoppdrag FROM (
                        SELECT fagsak_id, behandling_id, utbetalingsoppdrag, 
                          ROW_NUMBER() OVER (PARTITION BY fagsak_id, behandling_id ORDER BY versjon DESC) rn
                          FROM oppdrag_lager WHERE fagsystem=:fagsystem AND behandling_id IN (:behandlingIder)
                          AND status IN (:status)) q 
                        WHERE rn = 1"""

        val status = setOf(OppdragStatus.KVITTERT_OK, OppdragStatus.KVITTERT_MED_MANGLER).map { it.name }

        return behandlingIder.chunked(3000).map { behandlingIderChunked ->
            val values = MapSqlParameterSource()
                .addValue("fagsystem", fagsystem)
                .addValue("behandlingIder", behandlingIderChunked)
                .addValue("status", status)

            namedParameterJdbcTemplate.query(query, values) { resultSet, _ ->
                UtbetalingsoppdragForKonsistensavstemming(
                    resultSet.getString("fagsak_id"),
                    resultSet.getString("behandling_id"),
                    objectMapper.readValue(resultSet.getString("utbetalingsoppdrag")),
                )
            }
        }.flatten()
    }

    override fun hentSisteUtbetalingsoppdragForFagsaker(
        fagsystem: String,
        fagsakIder: Set<String>,
    ): List<UtbetalingsoppdragForKonsistensavstemming> {
        val sqlSpørring = """
            SELECT fagsak_id, behandling_id, utbetalingsoppdrag
            FROM oppdrag_lager
            WHERE (fagsak_id, opprettet_tidspunkt) IN (
                SELECT fagsak_id, MAX(opprettet_tidspunkt)
                FROM oppdrag_lager
                WHERE fagsystem=:fagsystem and fagsak_id IN (:fagsakIder)
                GROUP BY fagsak_id
            )
        """.trimIndent()

        return fagsakIder
            .chunked(3000)
            .flatMap { fagsakIderChunked ->
                val parametere = mapOf("fagsakIder" to fagsakIderChunked, "fagsystem" to fagsystem)

                namedParameterJdbcTemplate
                    .query(sqlSpørring, parametere) { resultSet, _ ->
                        UtbetalingsoppdragForKonsistensavstemming(
                            resultSet.getString("fagsak_id"),
                            resultSet.getString("behandling_id"),
                            objectMapper.readValue(resultSet.getString("utbetalingsoppdrag")),
                        )
                    }
            }
    }
}

class OppdragLagerRowMapper : RowMapper<OppdragLager> {

    override fun mapRow(resultSet: ResultSet, rowNumbers: Int): OppdragLager {
        return OppdragLager(
            uuid = UUID.fromString(resultSet.getString("id") ?: UUID.randomUUID().toString()),
            fagsystem = resultSet.getString("fagsystem"),
            personIdent = resultSet.getString("person_ident"),
            fagsakId = resultSet.getString("fagsak_id"),
            behandlingId = resultSet.getString("behandling_id"),
            utbetalingsoppdrag = objectMapper.readValue(resultSet.getString("utbetalingsoppdrag")),
            utgåendeOppdrag = resultSet.getString("utgaaende_oppdrag"),
            status = OppdragStatus.valueOf(resultSet.getString("status")),
            avstemmingTidspunkt = resultSet.getTimestamp("avstemming_tidspunkt").toLocalDateTime(),
            opprettetTidspunkt = resultSet.getTimestamp("opprettet_tidspunkt").toLocalDateTime(),
            kvitteringsmelding = resultSet.getString("kvitteringsmelding")?.let { objectMapper.readValue(it) },
            versjon = resultSet.getInt("versjon"),
        )
    }
}
