package no.nav.familie.oppdrag.grensesnittavstemming

import java.time.LocalDate

interface GrensesnittavstemmingSender {
    fun sendGrensesnittavstemming(dato: LocalDate)
}