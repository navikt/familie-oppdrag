package no.nav.familie.oppdrag.repository

interface SimuleringLagerTjeneste {

    fun lagreINyTransaksjon(simuleringLager: SimuleringLager)
    fun oppdater(simuleringLager: SimuleringLager)
    fun finnAlleSimuleringsLager(): List<SimuleringLager>
}
