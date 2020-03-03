package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.oppdrag.domene.OppdragId
import no.trygdeetaten.skjema.oppdrag.Mmel
import java.time.LocalDateTime

interface OppdragLagerRepository {

    fun hentOppdrag(oppdragId : OppdragId): OppdragLager
    fun hentUtbetalingsoppdrag(oppdragId: OppdragId): Utbetalingsoppdrag
    fun opprettOppdrag(oppdragLager: OppdragLager, versjon: Int = 0)
    fun oppdaterStatus(oppdragId: OppdragId, oppdragStatus: OppdragStatus)
    fun oppdaterKvitteringsmelding(oppdragId: OppdragId, kvittering: Mmel)
    fun hentIverksettingerForGrensesnittavstemming(fomTidspunkt: LocalDateTime, tomTidspunkt: LocalDateTime, fagOmråde: String): List<OppdragLager>
}