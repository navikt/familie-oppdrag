package no.nav.familie.oppdrag.domene

import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.trygdeetaten.skjema.oppdrag.Oppdrag

data class OppdragId(val fagsystem : String, val fødselsnummer : String, val bilagsnummer : String) {

}

fun Oppdrag.id() : OppdragId =
        OppdragId(this.oppdrag110.fagsystemId,
                  this.oppdrag110.oppdragGjelderId,
                  this.oppdrag110.oppdragsLinje150?.get(0)?.henvisning!!)

fun Utbetalingsoppdrag.id() : OppdragId =
        OppdragId(this.fagSystem,
                  this.aktoer,
                  this.utbetalingsperiode.get(0).behandlingId.toString())