package no.nav.familie.oppdrag.common

import jakarta.xml.ws.soap.SOAPFaultException
import org.slf4j.LoggerFactory

private val secureLogger = LoggerFactory.getLogger("secureLogger")

fun logSoapFaultException(e: Exception) {
    if (e is SOAPFaultException) {
        val details =
            e.fault.detail?.let { detail ->
                detail.detailEntries.asSequence().mapNotNull { it.textContent }.joinToString(",")
            }
        secureLogger.error(
            "SOAPFaultException -" +
                " faultCode=${e.fault.faultCode}" +
                " faultString=${e.fault.faultString}" +
                " details=$details",
        )
    }
}
