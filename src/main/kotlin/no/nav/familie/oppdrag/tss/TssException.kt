package no.nav.familie.oppdrag.tss

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


open class TssException() : RuntimeException() {
    lateinit var feilmelding: String
    var alvorligGrad: String? = null
    var kodeMelding: String? = null

    constructor(feilmelding: String) : this() {
        this.feilmelding = feilmelding
    }
    constructor(feilmelding: String, alvorligGrad: String?, kodeMelding: String?) : this() {
        this.feilmelding = feilmelding
        this.alvorligGrad = alvorligGrad
        this.kodeMelding = kodeMelding
    }
}

class TssResponseException(feilmelding: String, alvorligGrad: String?, kodeMelding: String?) : TssException(feilmelding, alvorligGrad, kodeMelding)


class TssConnectionException(feilmelding: String) : TssException(feilmelding)


class TssNoDataFoundException(feilmelding: String) : TssException(feilmelding)
