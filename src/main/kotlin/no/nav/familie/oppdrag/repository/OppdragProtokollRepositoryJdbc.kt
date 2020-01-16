package no.nav.familie.oppdrag.repository

import no.nav.familie.oppdrag.domene.OppdragId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class OppdragProtokollRepositoryJdbc(val jdbcTemplate: JdbcTemplate) : OppdragProtokollRepository {

    override fun hentOppdrag(oppdragId: OppdragId): List<OppdragProtokoll> {
        val hentStatement = "SELECT * FROM OPPDRAG_PROTOKOLL WHERE behandling_id = ? AND person_ident = ? AND fagsystem = ?"

        return jdbcTemplate.query(hentStatement,
                                  arrayOf(oppdragId.behandlingsId, oppdragId.personIdent, oppdragId.fagsystem),
                                  OppdragProtokollRowMapper())
    }

    override fun opprettOppdrag(oppdragProtokoll: OppdragProtokoll) {
        val insertStatement = "INSERT INTO oppdrag_protokoll VALUES (?,?,?,?,?,?,?,?,?)"

        jdbcTemplate.update(insertStatement,
                            oppdragProtokoll.melding,
                            oppdragProtokoll.status.name,
                            oppdragProtokoll.opprettetTidspunkt,
                            oppdragProtokoll.personIdent,
                            oppdragProtokoll.fagsakId,
                            oppdragProtokoll.behandlingId,
                            oppdragProtokoll.fagsystem,
                            oppdragProtokoll.avstemmingTidspunkt,
                            oppdragProtokoll.inputData)
    }

    override fun oppdaterStatus(oppdragId: OppdragId, oppdragProtokollStatus: OppdragProtokollStatus) {

        val update = "UPDATE oppdrag_protokoll SET status = '${oppdragProtokollStatus.name}' " +
                     "WHERE person_ident = '${oppdragId.personIdent}' " +
                     "AND fagsystem = '${oppdragId.fagsystem}' " +
                     "AND behandling_id = '${oppdragId.behandlingsId}'"

        jdbcTemplate.execute(update)
    }
}

class OppdragProtokollRowMapper : RowMapper<OppdragProtokoll> {

    override fun mapRow(resultSet: ResultSet, rowNumbers: Int): OppdragProtokoll? {
        return OppdragProtokoll(
                resultSet.getString(7),
                resultSet.getString(4),
                resultSet.getString(5),
                resultSet.getString(6),
                resultSet.getString(9),
                resultSet.getString(1),
                OppdragProtokollStatus.valueOf(resultSet.getString(2)),
                resultSet.getTimestamp(8).toLocalDateTime(),
                resultSet.getTimestamp(3).toLocalDateTime())
    }
}