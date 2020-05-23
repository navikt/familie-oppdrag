package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.oppdrag.iverksetting.Status
import no.trygdeetaten.skjema.oppdrag.Oppdrag

val Oppdrag.oppdragStatus: OppdragStatus
    get() {
        val kvitteringStatus = Status.fraKode(this.mmel?.alvorlighetsgrad ?: "Ukjent")

        return when (kvitteringStatus) {
            Status.OK -> OppdragStatus.KVITTERT_OK
            Status.AKSEPTERT_MEN_NOE_ER_FEIL -> OppdragStatus.KVITTERT_MED_MANGLER
            Status.AVVIST_FUNKSJONELLE_FEIL -> OppdragStatus.KVITTERT_FUNKSJONELL_FEIL
            Status.AVVIST_TEKNISK_FEIL -> OppdragStatus.KVITTERT_TEKNISK_FEIL
            Status.UKJENT -> OppdragStatus.KVITTERT_UKJENT
        }
    }