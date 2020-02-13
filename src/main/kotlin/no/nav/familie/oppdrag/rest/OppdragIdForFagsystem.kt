package no.nav.familie.oppdrag.rest

data class OppdragIdForFagsystem(val personIdent: String,
                                 val behandlingsId: Long) {
    override fun toString(): String = "OppdragId(behandlingsId=$behandlingsId)"
}