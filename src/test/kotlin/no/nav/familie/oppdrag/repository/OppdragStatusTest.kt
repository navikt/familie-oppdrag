package no.nav.familie.oppdrag.repository

import no.trygdeetaten.skjema.oppdrag.Mmel
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class OppdragStatusTest {


    @Test
    fun skal_konvertere_status() {

        assertEquals(OppdragStatus.KVITTERT_OK, lagOppdrag("00").protokollStatus)
        assertEquals(OppdragStatus.KVITTERT_MED_MANGLER, lagOppdrag("04").protokollStatus)
        assertEquals(OppdragStatus.KVITTERT_FUNKSJONELL_FEIL, lagOppdrag("08").protokollStatus)
        assertEquals(OppdragStatus.KVITTERT_TEKNISK_FEIL, lagOppdrag("12").protokollStatus)
        assertEquals(OppdragStatus.KVITTERT_UKJENT, lagOppdrag("Ukjent").protokollStatus)
    }

    private fun lagOppdrag(alvorlighetsgrad: String) : Oppdrag {
        val oppdrag = Oppdrag()
        oppdrag.mmel = Mmel()
        oppdrag.mmel.alvorlighetsgrad = alvorlighetsgrad
        return oppdrag
    }
}