package no.nav.familie.oppdrag.tss

open class TssException(
    feilmelding: String,
    var alvorligGrad: String? = null,
    var kodeMelding: String? = null,
    throwable: Throwable? = null,
) :
    RuntimeException(
            listOfNotNull(feilmelding, alvorligGrad, kodeMelding).joinToString("-"),
            throwable,
        )

class TssResponseException(feilmelding: String, alvorligGrad: String?, kodeMelding: String?) : TssException(
    feilmelding,
    alvorligGrad,
    kodeMelding,
)

class TssConnectionException(feilmelding: String, throwable: Throwable? = null) : TssException(feilmelding, throwable = throwable)

class TssNoDataFoundException(feilmelding: String) : TssException(feilmelding)
