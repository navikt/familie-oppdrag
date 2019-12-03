package no.nav.familie.oppdrag.iverksetting

import no.trygdeetaten.skjema.oppdrag.TfradragTillegg
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar


class OppdragSkjemaConstants {

    companion object {
        val OPPDRAG_GJELDER_DATO_FOM = LocalDate.of(2000, 1, 1)
        const val KODE_AKSJON = "1"

        const val ENHET_TYPE = "BOS"
        const val ENHET = "8020"
        val ENHET_DATO_FOM = LocalDate.of(1900, 1, 1)

        val FRADRAG_TILLEGG = TfradragTillegg.T
        const val BRUK_KJØREPLAN = "N"
    }
}

enum class EndringsKode(val kode: String) {
    NY("NY"),
    UENDRET("UEND"),
    ENDRING("ENDR")
}

enum class UtbetalingsfrekvensKode(val kode: String) {
    DAGLIG("DAG"),
    UKENTLIG("UKE"),
    MÅNEDLIG("MND"),
    DAGLIG_14("14DG"),
    ENGANGSUTBETALING("ENG")
}

enum class SatsTypeKode(val kode: String) {
    DAGLIG("DAG"),
    UKENTLIG("UKE"),
    MÅNEDLIG("MND"),
    DAGLIG_14("14DG"),
    ENGANGSBELØP("ENG"),
    ÅRLIG("AAR"),
    A_KONTO("AKTO"),
    UKJENT("-");
}

enum class GradTypeKode(val kode: String) {
    UFØREGRAD("UFOR"),
    UTBETALINGSGRAD("UBGR"),
    UTTAKSGRAD_ALDERSPENSJON("UTAP"),
    UTTAKSGRAD_AFP("AFPG")

}

fun LocalDate.toXMLDate(): XMLGregorianCalendar {
    return DatatypeFactory.newInstance()
            .newXMLGregorianCalendar(GregorianCalendar.from(atStartOfDay(ZoneId.systemDefault())))
}