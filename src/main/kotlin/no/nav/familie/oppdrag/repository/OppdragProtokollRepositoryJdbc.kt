package no.nav.familie.oppdrag.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class OppdragProtokollRepositoryJdbc(val jdbcTemplate: JdbcTemplate) : OppdragProtokollRepository {

    override fun hentOppdrag(fagsystem: String, behandlingId: String, personIdent: String): List<OppdragProtokoll> {
        val hentStatement = "SELECT * FROM OPPDRAG_PROTOKOLL WHERE behandling_id = ? AND person_ident = ? AND fagsystem = ?"

        return jdbcTemplate.query(hentStatement,
                    arrayOf(behandlingId, personIdent, fagsystem),
                    OppdragProtokollRowMapper())
    }

    override fun lagreOppdrag(oppdragProtokoll: OppdragProtokoll) {
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

}

class OppdragProtokollRowMapper: RowMapper<OppdragProtokoll> {

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