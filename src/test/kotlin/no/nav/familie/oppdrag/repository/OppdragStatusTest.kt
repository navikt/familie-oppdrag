package no.nav.familie.oppdrag.repository

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.trygdeetaten.skjema.oppdrag.Mmel
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class OppdragStatusTest {

    @Test
    fun skal_konvertere_status() {

        assertEquals(OppdragStatus.KVITTERT_OK, lagOppdrag("00").oppdragStatus)
        assertEquals(OppdragStatus.KVITTERT_MED_MANGLER, lagOppdrag("04").oppdragStatus)
        assertEquals(OppdragStatus.KVITTERT_FUNKSJONELL_FEIL, lagOppdrag("08").oppdragStatus)
        assertEquals(OppdragStatus.KVITTERT_TEKNISK_FEIL, lagOppdrag("12").oppdragStatus)
        assertEquals(OppdragStatus.KVITTERT_UKJENT, lagOppdrag("Ukjent").oppdragStatus)
    }

    private fun lagOppdrag(alvorlighetsgrad: String): Oppdrag {
        val oppdrag = Oppdrag()
        oppdrag.mmel = Mmel()
        oppdrag.mmel.alvorlighetsgrad = alvorlighetsgrad
        return oppdrag
    }
}
