package no.nav.familie.oppdrag.repository

interface OppdragProtokollRepository {

    fun hentOppdrag(fagsystem: String, behandlingId: String, personIdent: String): List<OppdragProtokoll>
    fun lagreOppdrag(oppdragProtokoll: OppdragProtokoll)
}