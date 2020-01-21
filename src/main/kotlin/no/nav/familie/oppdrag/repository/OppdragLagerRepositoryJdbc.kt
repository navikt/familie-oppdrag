package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.oppdrag.domene.OppdragId
import no.trygdeetaten.skjema.oppdrag.Mmel
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime

@Repository
class OppdragLagerRepositoryJdbc(val jdbcTemplate: JdbcTemplate) : OppdragLagerRepository {

    internal var LOG = LoggerFactory.getLogger(OppdragLagerRepositoryJdbc::class.java)

    override fun hentOppdrag(oppdragId: OppdragId): OppdragLager {
        val hentStatement = "SELECT * FROM oppdrag_lager WHERE behandling_id = ? AND person_ident = ? AND fagsystem = ?"

        val listeAvOppdrag = jdbcTemplate.query(hentStatement,
                                  arrayOf(oppdragId.behandlingsId, oppdragId.personIdent, oppdragId.fagsystem),
                                  OppdragLagerRowMapper())

        return when( listeAvOppdrag.size ) {
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

    override fun opprettOppdrag(oppdragLager: OppdragLager) {
        val insertStatement = "INSERT INTO oppdrag_lager VALUES (?,?,?,?,?,?,?,?,?)"

        jdbcTemplate.update(insertStatement,
                            oppdragLager.utgåendeOppdrag,
                            oppdragLager.status.name,
                            oppdragLager.opprettetTidspunkt,
                            oppdragLager.personIdent,
                            oppdragLager.fagsakId,
                            oppdragLager.behandlingId,
                            oppdragLager.fagsystem,
                            oppdragLager.avstemmingTidspunkt,
                            oppdragLager.utbetalingsoppdrag)
    }

    override fun oppdaterStatus(oppdragId: OppdragId, oppdragStatus: OppdragStatus) {

        val update = "UPDATE oppdrag_lager SET status = '${oppdragStatus.name}' " +
                     "WHERE person_ident = '${oppdragId.personIdent}' " +
                     "AND fagsystem = '${oppdragId.fagsystem}' " +
                     "AND behandling_id = '${oppdragId.behandlingsId}'"

        jdbcTemplate.execute(update)
    }

    override fun oppdaterKvitteringsmelding(oppdragId: OppdragId, kvittering: Mmel) {
        val updateStatement = "UPDATE oppdrag_lager SET kvitteringsmelding = ? WHERE person_ident = ? AND fagsystem = ? AND behandling_id = ?"

        jdbcTemplate.update(updateStatement,
                objectMapper.writeValueAsString(kvittering),
                oppdragId.personIdent,
                oppdragId.fagsystem,
                oppdragId.behandlingsId)
    }

    override fun hentIverksettingerForGrensesnittavstemming(fomTidspunkt: LocalDateTime, tomTidspunkt: LocalDateTime, fagOmråde: String): List<OppdragLager> {
        val hentStatement = "SELECT * FROM OPPDRAG_PROTOKOLL WHERE avstemming_tidspunkt >= ? AND avstemming_tidspunkt < ? AND fagsystem = ?"

        return jdbcTemplate.query(hentStatement,
                arrayOf(fomTidspunkt, tomTidspunkt, fagOmråde),
                OppdragLagerRowMapper())
    }

}

class OppdragLagerRowMapper : RowMapper<OppdragLager> {

    override fun mapRow(resultSet: ResultSet, rowNumbers: Int): OppdragLager? {
        return OppdragLager(
                resultSet.getString(7),
                resultSet.getString(4),
                resultSet.getString(5),
                resultSet.getString(6),
                resultSet.getString(9),
                resultSet.getString(1),
                OppdragStatus.valueOf(resultSet.getString(2)),
                resultSet.getTimestamp(8).toLocalDateTime(),
                resultSet.getTimestamp(3).toLocalDateTime(),
                resultSet.getString(10))
    }
}