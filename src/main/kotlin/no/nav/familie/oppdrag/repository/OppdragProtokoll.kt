package no.nav.familie.oppdrag.repository

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class OppdragProtokoll(@Id val serienummer: Long = 0,
                            val fagsystem: String,
                            @Column("person_id") val personId: String,
                            @Column("fagsak_id") val fagsakId: String,
                            @Column("behandling_id") val behandlingId: String,
                            @Column("input_data") val inputData: String,
                            val melding: String,
                            val status: OppdragProtokollStatus = OppdragProtokollStatus.LAGT_PÅ_KØ,
                            @Column("opprettet_tidspunkt") val opprettetTidspunkt: LocalDateTime = LocalDateTime.now()) {

    companion object {
        fun lagFraOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag, oppdrag : Oppdrag) : OppdragProtokoll {
            return OppdragProtokoll(
                    personId = utbetalingsoppdrag.aktoer,
                    fagsystem = utbetalingsoppdrag.fagSystem,
                    fagsakId = utbetalingsoppdrag.saksnummer,
                    behandlingId = utbetalingsoppdrag.utbetalingsperiode[0].behandlingId.toString(),
                    inputData = ObjectMapper().writeValueAsString(utbetalingsoppdrag),
                    melding = ObjectMapper().writeValueAsString(oppdrag)
            )
        }
    }
}
