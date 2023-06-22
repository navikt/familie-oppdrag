package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.behandlingsIdForFørsteUtbetalingsperiode
import java.time.LocalDateTime

val Utbetalingsoppdrag.somKvitteringsinformasjon: Kvitteringsinformasjon
    get() {
        return Kvitteringsinformasjon(
            fagsystem = this.fagSystem,
            personIdent = this.aktoer,
            fagsakId = this.saksnummer,
            behandlingId = this.behandlingsIdForFørsteUtbetalingsperiode(),
            avstemmingTidspunkt = this.avstemmingTidspunkt,
            kvitteringsmelding = null,
            opprettetTidspunkt = LocalDateTime.now(),
            status = OppdragStatus.LAGT_PÅ_KØ,
            versjon = 0,
        )
    }
