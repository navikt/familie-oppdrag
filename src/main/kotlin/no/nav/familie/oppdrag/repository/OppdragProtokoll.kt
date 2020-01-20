package no.nav.familie.oppdrag.repository

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppdrag.behandlingsIdForFørsteUtbetalingsperiode
import no.nav.familie.oppdrag.domene.OppdragId
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class OppdragProtokoll(val fagsystem: String,
                            @Column("person_ident") val personIdent: String,
                            @Column("fagsak_id") val fagsakId: String,
                            @Column("behandling_id") val behandlingId: String,
                            @Column("input_data") val inputData: String,
                            val melding: String,
                            val status: OppdragProtokollStatus = OppdragProtokollStatus.LAGT_PÅ_KØ,
                            @Column("avstemming_tidspunkt") val avstemmingTidspunkt: LocalDateTime,
                            @Column("opprettet_tidspunkt") val opprettetTidspunkt: LocalDateTime = LocalDateTime.now()) {

    companion object {
        fun lagFraOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag, oppdrag: Oppdrag): OppdragProtokoll {
            return OppdragProtokoll(
                    personIdent = utbetalingsoppdrag.aktoer,
                    fagsystem = utbetalingsoppdrag.fagSystem,
                    fagsakId = utbetalingsoppdrag.saksnummer,
                    behandlingId = utbetalingsoppdrag.behandlingsIdForFørsteUtbetalingsperiode(),
                    avstemmingTidspunkt = utbetalingsoppdrag.avstemmingTidspunkt,
                    inputData = objectMapper.writeValueAsString(utbetalingsoppdrag),
                    melding = ObjectMapper().writeValueAsString(oppdrag)
            )
        }
    }

}

val Utbetalingsoppdrag.somOppdragProtokoll: OppdragProtokoll
    get() {
        val tilOppdrag110 = OppdragMapper().tilOppdrag110(this)
        val oppdrag = OppdragMapper().tilOppdrag(tilOppdrag110);
        return OppdragProtokoll.lagFraOppdrag(this, oppdrag)
    }

val OppdragProtokoll.id : OppdragId
    get() {
        return OppdragId(this.fagsystem,this.personIdent,this.behandlingId)
    }