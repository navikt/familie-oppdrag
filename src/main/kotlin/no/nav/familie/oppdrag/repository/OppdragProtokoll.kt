package no.nav.familie.oppdrag.repository

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.oppdrag.domene.OppdragId
import no.nav.familie.oppdrag.domene.id
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import javax.persistence.IdClass

@IdClass(OppdragId::class)
data class OppdragProtokoll(@Id val fagsystem : String,
                            @Id val fødselsnummer : String,
                            @Id val behandlingsId : String,
                            val melding: String,
                            val status: OppdragProtokollStatus = OppdragProtokollStatus.LAGT_PÅ_KØ,
                            @Column("opprettet_tidspunkt") val opprettetTidspunkt: LocalDateTime = LocalDateTime.now()) {

    companion object {
        fun lagFraOppdrag(oppdrag : Oppdrag) : OppdragProtokoll {
            return OppdragProtokoll(
                    fagsystem = oppdrag.id().fagsystem,
                    fødselsnummer = oppdrag.id().fødselsnummer,
                    behandlingsId = oppdrag.id().behandlingsId,
                    melding = ObjectMapper().writeValueAsString(oppdrag)
            )
        }
    }
}
