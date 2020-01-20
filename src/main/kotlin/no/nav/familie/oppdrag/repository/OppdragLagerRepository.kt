package no.nav.familie.oppdrag.repository

import no.nav.familie.oppdrag.domene.OppdragId

interface OppdragLagerRepository {

    fun hentOppdrag(oppdragId : OppdragId): OppdragLager
    fun opprettOppdrag(oppdragLager: OppdragLager)
    fun oppdaterStatus(oppdragId: OppdragId, oppdragStatus: OppdragStatus)
}