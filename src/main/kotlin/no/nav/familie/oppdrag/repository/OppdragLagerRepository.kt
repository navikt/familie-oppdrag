package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.trygdeetaten.skjema.oppdrag.Mmel
import java.time.LocalDateTime

interface OppdragLagerRepository {

    fun hentOppdrag(oppdragId : OppdragId, versjon: Int = 0): OppdragLager
    fun hentUtbetalingsoppdrag(oppdragId: OppdragId, versjon: Int = 0): Utbetalingsoppdrag
    fun hentAlleVersjonerAvOppdrag(oppdragId: OppdragId): List<OppdragLager>
    fun opprettOppdrag(oppdragLager: OppdragLager, versjon: Int = 0)
    fun oppdaterStatus(oppdragId: OppdragId, oppdragStatus: OppdragStatus, versjon: Int = 0)
    fun oppdaterKvitteringsmelding(oppdragId: OppdragId, kvittering: Mmel, versjon: Int = 0)
    fun hentIverksettingerForGrensesnittavstemming(fomTidspunkt: LocalDateTime, tomTidspunkt: LocalDateTime, fagOmråde: String): List<OppdragLager>
}