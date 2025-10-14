package no.nav.familie.oppdrag.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val mapper = jacksonObjectMapper()

/**
 * Lager en kopi av et objekt. Nyttig når man jobber med objekter som ikke er dataklasser og dermed ikke har en .copy()-metode
 */
fun <T> T.deepClone(): T {
    val json = mapper.writeValueAsString(this)
    return mapper.readValue(json, this!!::class.java)
}