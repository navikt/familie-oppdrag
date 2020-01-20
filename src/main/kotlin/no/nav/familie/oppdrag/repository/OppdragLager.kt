package no.nav.familie.oppdrag.repository

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.behandlingsIdForFørsteUtbetalingsperiode
import no.nav.familie.oppdrag.domene.OppdragId
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class OppdragLager(val fagsystem: String,
                        @Column("person_ident") val personIdent: String,
                        @Column("fagsak_id") val fagsakId: String,
                        @Column("behandling_id") val behandlingId: String,
                        @Column("input_data") val inputData: String,
                        val melding: String,
                        val status: OppdragStatus = OppdragStatus.LAGT_PÅ_KØ,
                        @Column("avstemming_tidspunkt") val avstemmingTidspunkt: LocalDateTime,
                        @Column("opprettet_tidspunkt") val opprettetTidspunkt: LocalDateTime = LocalDateTime.now()) {

    companion object {
        fun lagFraOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag, oppdrag: Oppdrag): OppdragLager {
            return OppdragLager(
                    personIdent = utbetalingsoppdrag.aktoer,
                    fagsystem = utbetalingsoppdrag.fagSystem,
                    fagsakId = utbetalingsoppdrag.saksnummer,
                    behandlingId = utbetalingsoppdrag.behandlingsIdForFørsteUtbetalingsperiode(),
                    avstemmingTidspunkt = utbetalingsoppdrag.avstemmingTidspunkt,
                    inputData = ObjectMapper().writeValueAsString(utbetalingsoppdrag),
                    melding = ObjectMapper().writeValueAsString(oppdrag)
            )
        }
    }

}

val Utbetalingsoppdrag.somOppdragLager: OppdragLager
    get() {
        val tilOppdrag110 = OppdragMapper().tilOppdrag110(this)
        val oppdrag = OppdragMapper().tilOppdrag(tilOppdrag110);
        return OppdragLager.lagFraOppdrag(this, oppdrag)
    }

val OppdragLager.id : OppdragId
    get() {
        return OppdragId(this.fagsystem,this.personIdent,this.behandlingId)
    }