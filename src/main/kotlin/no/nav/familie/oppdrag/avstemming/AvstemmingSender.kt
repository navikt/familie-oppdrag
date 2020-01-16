package no.nav.familie.oppdrag.avstemming

import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Avstemmingsdata

interface AvstemmingSender {
    fun sendGrensesnittAvstemming(avstemmingsdata: Avstemmingsdata)
}