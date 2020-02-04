package no.nav.familie.oppdrag.konsistensavstemming

import no.nav.familie.oppdrag.util.TestOppdragMedAvstemmingsdato
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.time.LocalDateTime
import kotlin.test.assertEquals

class KonsistensavstemmingMapperTest {
    val fagområde = "BA"
    val idag = LocalDateTime.now()

    @Test
    fun testMappingTilKonsistensavstemming() {
        val utbetalingsoppdrag = TestOppdragMedAvstemmingsdato.lagTestUtbetalingsoppdrag(idag, fagområde)
        val mapper = KonsistensavstemmingMapper(fagområde, listOf(utbetalingsoppdrag), idag)
        val meldinger = mapper.lagAvstemmingsmeldinger()
        assertEquals(4, meldinger.size)
        assertEquals(KonsistensavstemmingConstants.START, meldinger.first().aksjonsdata.aksjonsType)
        assertEquals(KonsistensavstemmingConstants.DATA, meldinger[1].aksjonsdata.aksjonsType)
        assertEquals(KonsistensavstemmingConstants.DATA, meldinger[2].aksjonsdata.aksjonsType)
        assertEquals(BigInteger.ONE, meldinger[2].totaldata.totalAntall)
        assertEquals(KonsistensavstemmingConstants.AVSLUTT, meldinger.last().aksjonsdata.aksjonsType)

    }
}