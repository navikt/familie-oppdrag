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

 }


    fun Oppdrag.tilOppdragProtokoll() : OppdragProtokoll {
        return OppdragProtokoll(
                fagsystem = this.id.fagsystem,
                fødselsnummer = this.id.fødselsnummer,
                behandlingsId = this.id.behandlingsId,
                melding = ObjectMapper().writeValueAsString(this)
        )
    }

