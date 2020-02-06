package no.nav.familie.oppdrag.avstemming

import java.nio.ByteBuffer
import java.util.*

class AvstemmingMapper {

    fun encodeUUIDBase64(uuid: UUID): String {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return Base64.getUrlEncoder().encodeToString(bb.array()).substring(0, 22)
    }
}

enum class SystemKode(val kode : String) {
    OPPDRAGSSYSTEMET("OS")
}