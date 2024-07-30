package no.nav.familie.oppdrag.domene

import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.trygdeetaten.skjema.oppdrag.Oppdrag

val Oppdrag.id: OppdragId
    get() =
        OppdragId(
            this.oppdrag110.kodeFagomraade,
            this.oppdrag110.oppdragGjelderId,
            this.oppdrag110.oppdragsLinje150
                ?.get(0)
                ?.henvisning!!,
        )
