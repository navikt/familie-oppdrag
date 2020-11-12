package no.nav.familie.oppdrag.service

import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragRequest
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag

interface OppdragService {
    fun opprettOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag, versjon: Int)
    fun opprettOppdragV2(oppdragRequest: OppdragRequest, versjon: Int)
    fun hentStatusForOppdrag(oppdragId: OppdragId): OppdragStatus
}