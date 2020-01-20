package no.nav.familie.oppdrag.repository

import java.time.LocalDateTime

import no.nav.familie.oppdrag.domene.OppdragId

interface OppdragProtokollRepository {

    fun hentOppdrag(oppdragId : OppdragId): OppdragProtokoll
    fun opprettOppdrag(oppdragProtokoll: OppdragProtokoll)
    fun oppdaterStatus(oppdragId: OppdragId, oppdragProtokollStatus: OppdragProtokollStatus)
    fun hentIverksettingerForGrensesnittavstemming(fomTidspunkt: LocalDateTime, tomTidspunkt: LocalDateTime, fagOmråde: String): List<OppdragProtokoll>

}