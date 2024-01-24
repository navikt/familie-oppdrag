package no.nav.familie.oppdrag.avstemming

import no.nav.virksomhet.tjenester.avstemming.informasjon.konsistensavstemmingsdata.v1.Konsistensavstemmingsdata
import no.nav.virksomhet.tjenester.avstemming.meldinger.v1.Avstemmingsdata

interface AvstemmingSender {
    fun sendGrensesnittAvstemming(avstemmingsdata: Avstemmingsdata)

    fun sendKonsistensAvstemming(avstemmingsdata: Konsistensavstemmingsdata)
}
