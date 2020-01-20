package no.nav.familie.oppdrag.repository

import no.nav.familie.oppdrag.domene.OppdragId
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime

@Repository
class OppdragProtokollRepositoryJdbc(val jdbcTemplate: JdbcTemplate) : OppdragProtokollRepository {
    internal var LOG = LoggerFactory.getLogger(OppdragProtokollRepositoryJdbc::class.java)

    override fun hentOppdrag(oppdragId: OppdragId): OppdragProtokoll {
        val hentStatement = "SELECT * FROM OPPDRAG_PROTOKOLL WHERE behandling_id = ? AND person_ident = ? AND fagsystem = ?"

        val listeAvOppdrag = jdbcTemplate.query(hentStatement,
                                  arrayOf(oppdragId.behandlingsId, oppdragId.personIdent, oppdragId.fagsystem),
                                  OppdragProtokollRowMapper())

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

    override fun hentIverksettingerForGrensesnittavstemming(fomTidspunkt: LocalDateTime, tomTidspunkt: LocalDateTime, fagOmråde: String): List<OppdragProtokoll> {
        val hentStatement = "SELECT * FROM OPPDRAG_PROTOKOLL WHERE avstemming_tidspunkt >= ? AND avstemming_tidspunkt < ? AND fagsystem = ?"

        return jdbcTemplate.query(hentStatement,
                arrayOf(fomTidspunkt, tomTidspunkt, fagOmråde),
                OppdragProtokollRowMapper())
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