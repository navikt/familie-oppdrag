package no.nav.familie.oppdrag.iverksetting

import no.trygdeetaten.skjema.oppdrag.Oppdrag
import java.lang.IllegalArgumentException

@Suppress("ktlint:trailing-comma-on-declaration-site")
enum class Status(val kode: String) {
    OK("00"),
    AKSEPTERT_MEN_NOE_ER_FEIL("04"),
    AVVIST_FUNKSJONELLE_FEIL("08"),
    AVVIST_TEKNISK_FEIL("12"),
    UKJENT("Ukjent");

    companion object {
        fun fraKode(kode: String): Status {
            values().forEach {
                if (it.kode == kode) return it
            }
            throw IllegalArgumentException("No enum constant with kode=$kode")
        }
    }
}

val Oppdrag.status: Status
    get() = Status.fraKode(this.mmel?.alvorlighetsgrad ?: "Ukjent")
