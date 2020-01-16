package no.nav.familie.oppdrag.repository

import java.time.LocalDateTime

interface OppdragProtokollRepository {

    fun hentOppdrag(fagsystem: String, behandlingId: String, personIdent: String): List<OppdragProtokoll>
    fun lagreOppdrag(oppdragProtokoll: OppdragProtokoll)
    fun hentIverksettingerForGrensesnittavstemming(fomTidspunkt: LocalDateTime, tomTidspunkt: LocalDateTime, fagOmråde: String): List<OppdragProtokoll>

}