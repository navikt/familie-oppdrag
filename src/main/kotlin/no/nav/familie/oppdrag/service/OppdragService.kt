package no.nav.familie.oppdrag.service

import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.rest.RestSendOppdrag
import no.trygdeetaten.skjema.oppdrag.Oppdrag

interface OppdragService {
    fun opprettOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag, versjon: Int)
    fun opprettOppdragV2(restSendOppdrag: RestSendOppdrag, versjon: Int)
    fun hentStatusForOppdrag(oppdragId: OppdragId): OppdragStatus
}