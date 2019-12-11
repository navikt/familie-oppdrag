package no.nav.familie.oppdrag.iverksetting

import java.lang.IllegalArgumentException

enum class Status(val kode: String) {
    OK("00"),
    AKSEPTERT_MEN_NOE_ER_FEIL("04"),
    AVVIST_FUNKSJONELLE_FEIL("08"),
    AVVIST_TEKNISK_FEIL("12");

    companion object {
        fun fraKode(kode: String): Status {
            values().forEach {
                if (it.kode == kode) return it
            }
            throw IllegalArgumentException("No enum constant with kode=$kode")
        }
    }
}

