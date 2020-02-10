package no.nav.familie.oppdrag.rest

data class OppdragIdForFagsystem(val personIdent: String,
                                 val behandlingsId: String) {
    override fun toString(): String = "OppdragId(behandlingsId=$behandlingsId)"
}