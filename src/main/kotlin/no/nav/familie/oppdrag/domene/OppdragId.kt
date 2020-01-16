package no.nav.familie.oppdrag.domene

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.behandlingsIdForFørsteUtbetalingsperiode
import no.trygdeetaten.skjema.oppdrag.Oppdrag

data class OppdragId(val fagsystem : String,
                     val personIdent : String,
                     val behandlingsId : String)

val Oppdrag.id : OppdragId
    get() = OppdragId(this.oppdrag110.kodeFagomraade,
                      this.oppdrag110.oppdragGjelderId,
                      this.oppdrag110.oppdragsLinje150?.get(0)?.henvisning!!)

val Utbetalingsoppdrag.id : OppdragId
    get() = OppdragId(this.fagSystem,
                      this.aktoer,
                      this.behandlingsIdForFørsteUtbetalingsperiode())