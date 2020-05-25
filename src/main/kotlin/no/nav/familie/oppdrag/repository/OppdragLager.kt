package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.behandlingsIdForFørsteUtbetalingsperiode
import no.nav.familie.oppdrag.iverksetting.Jaxb
import no.nav.familie.oppdrag.iverksetting.OppdragMapper
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class OppdragLager(val fagsystem: String,
                        @Column("person_ident") val personIdent: String,
                        @Column("fagsak_id") val fagsakId: String,
                        @Column("behandling_id") val behandlingId: String,
                        val utbetalingsoppdrag: String,
                        @Column("utgaaende_oppdrag") val utgåendeOppdrag: String,
                        var status: OppdragStatus = OppdragStatus.LAGT_PÅ_KØ,
                        @Column("avstemming_tidspunkt") val avstemmingTidspunkt: LocalDateTime,
                        @Column("opprettet_tidspunkt") val opprettetTidspunkt: LocalDateTime = LocalDateTime.now(),
                        val kvitteringsmelding: String?,
                        val versjon: Int = 0) {

    companion object {
        fun lagFraOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag, oppdrag: Oppdrag, versjon: Int = 0): OppdragLager {
            return OppdragLager(
                    personIdent = utbetalingsoppdrag.aktoer,
                    fagsystem = utbetalingsoppdrag.fagSystem,
                    fagsakId = utbetalingsoppdrag.saksnummer,
                    behandlingId = utbetalingsoppdrag.behandlingsIdForFørsteUtbetalingsperiode(),
                    avstemmingTidspunkt = utbetalingsoppdrag.avstemmingTidspunkt,
                    utbetalingsoppdrag = objectMapper.writeValueAsString(utbetalingsoppdrag),
                    utgåendeOppdrag = Jaxb.tilXml(oppdrag),
                    kvitteringsmelding = null,
                    versjon = versjon
            )
        }
    }

}

fun Utbetalingsoppdrag.somOppdragLagerMedVersjon(versjon: Int): OppdragLager {
    val tilOppdrag110 = OppdragMapper().tilOppdrag110(this)
    val oppdrag = OppdragMapper().tilOppdrag(tilOppdrag110)
    return OppdragLager.lagFraOppdrag(this, oppdrag, versjon)
}

val Utbetalingsoppdrag.somOppdragLager: OppdragLager
    get() {
        val tilOppdrag110 = OppdragMapper().tilOppdrag110(this)
        val oppdrag = OppdragMapper().tilOppdrag(tilOppdrag110)
        return OppdragLager.lagFraOppdrag(this, oppdrag)
    }

val OppdragLager.id : OppdragId
    get() {
        return OppdragId(this.fagsystem,this.personIdent,this.behandlingId)
    }