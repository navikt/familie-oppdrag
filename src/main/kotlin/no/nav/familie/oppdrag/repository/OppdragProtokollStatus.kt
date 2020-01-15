package no.nav.familie.oppdrag.repository

import no.nav.familie.oppdrag.domene.OppdragId
import no.nav.familie.oppdrag.iverksetting.Status
import no.trygdeetaten.skjema.oppdrag.Oppdrag

enum class OppdragProtokollStatus {
    LAGT_PÅ_KØ, KVITTERT_OK, KVITTERT_MED_MANGLER, KVITTERT_FUNKSJONELL_FEIL, KVITTERT_TEKNISK_FEIL, KVITTERT_UKJENT
}

val Oppdrag.protokollStatus: OppdragProtokollStatus
    get() {
        val status = Status.fraKode(this.mmel?.alvorlighetsgrad ?: "Ukjent")

        return when (status) {
            Status.OK -> OppdragProtokollStatus.KVITTERT_OK
            Status.AKSEPTERT_MEN_NOE_ER_FEIL -> OppdragProtokollStatus.KVITTERT_MED_MANGLER
            Status.AVVIST_FUNKSJONELLE_FEIL -> OppdragProtokollStatus.KVITTERT_FUNKSJONELL_FEIL
            Status.AVVIST_TEKNISK_FEIL -> OppdragProtokollStatus.KVITTERT_TEKNISK_FEIL
            Status.UKJENT -> OppdragProtokollStatus.KVITTERT_UKJENT
        }

    }