package no.nav.familie.oppdrag.repository

import no.nav.familie.oppdrag.domene.OppdragId

interface OppdragProtokollRepository {

    fun hentOppdrag(oppdragId : OppdragId): List<OppdragProtokoll>
    fun opprettOppdrag(oppdragProtokoll: OppdragProtokoll)
    fun oppdaterStatus(oppdragId: OppdragId, oppdragProtokollStatus: OppdragProtokollStatus)
}