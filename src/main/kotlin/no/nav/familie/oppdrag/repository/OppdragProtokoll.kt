package no.nav.familie.oppdrag.repository

import com.fasterxml.jackson.databind.ObjectMapper
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class OppdragProtokoll(@Id val serienummer: Long = 0,
                            val id : String,
                            val melding: String,
                            val status: OppdragProtokollStatus = OppdragProtokollStatus.LAGT_PÅ_KØ,
                            @Column("opprettet_tidspunkt") val opprettetTidspunkt: LocalDateTime = LocalDateTime.now()) {

    companion object {
        fun lagFraOppdrag(oppdrag : Oppdrag) : OppdragProtokoll {
            return OppdragProtokoll(
                    id = "id", // TODO Plukk "noe" fra oppdrag
                    melding = ObjectMapper().writeValueAsString(oppdrag)
            )
        }
    }
}
