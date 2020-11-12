package no.nav.familie.oppdrag.avstemming

import java.nio.ByteBuffer
import java.util.*

object AvstemmingMapper {

    fun encodeUUIDBase64(uuid: UUID): String {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return Base64.getUrlEncoder().encodeToString(bb.array()).substring(0, 22)
    }

    fun fagområdeTilAvleverendeKomponentKode(fagområde: String): String {
        return when(fagområde) {
            "EFOG" -> "EF"
            "BA" -> "BA"
            else -> throw Error("Grensesnittavstemming støttes ikke for $fagområde")
        }
    }
}

enum class SystemKode(val kode : String) {
    OPPDRAGSSYSTEMET("OS")
}