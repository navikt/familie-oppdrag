package no.nav.familie.oppdrag.tss

open class TssException(feilmelding: String, var alvorligGrad: String? = null, var kodeMelding: String? = null) :
    RuntimeException(listOfNotNull(feilmelding, alvorligGrad, kodeMelding).joinToString { "-" })

class TssResponseException(feilmelding: String, alvorligGrad: String?, kodeMelding: String?) : TssException(feilmelding, alvorligGrad, kodeMelding)

class TssConnectionException(feilmelding: String) : TssException(feilmelding)

class TssNoDataFoundException(feilmelding: String) : TssException(feilmelding)
