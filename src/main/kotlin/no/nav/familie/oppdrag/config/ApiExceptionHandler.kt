package no.nav.familie.oppdrag.config

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.oppdrag.common.RessursUtils.illegalState
import no.nav.familie.oppdrag.common.RessursUtils.serviceUnavailable
import no.nav.familie.oppdrag.common.RessursUtils.unauthorized
import no.nav.familie.oppdrag.tss.TssConnectionException
import no.nav.familie.oppdrag.tss.TssException
import no.nav.familie.oppdrag.tss.TssNoDataFoundException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils.getMostSpecificCause
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleJwtTokenUnauthorizedException(
        jwtTokenUnauthorizedException: JwtTokenUnauthorizedException,
    ): ResponseEntity<Ressurs<Nothing>> = unauthorized("Unauthorized")

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ResponseEntity<Ressurs<Nothing>> {
        val mostSpecificThrowable = getMostSpecificCause(throwable)
        return illegalState(mostSpecificThrowable.message.toString(), mostSpecificThrowable)
    }

    @ExceptionHandler(IntegrasjonException::class)
    fun handleThrowable(feil: IntegrasjonException): ResponseEntity<Ressurs<Nothing>> {
        when (feil.system) {
            Integrasjonssystem.TILBAKEKREVING -> {
                secureLogger.warn("Feil mot oppdrag ved ${feil.system} har oppstått", feil)
                logger.warn("Feil mot oppdrag ved ${feil.system} har oppstått exception=${getMostSpecificCause(feil)::class}")
            }
            else -> {
                secureLogger.error("Feil mot oppdrag ved ${feil.system} har oppstått", feil)
                logger.error("Feil mot oppdrag ved ${feil.system} har oppstått exception=${getMostSpecificCause(feil)::class}")
            }
        }

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Ressurs.failure(errorMessage = feil.message))
    }

    @ExceptionHandler(TssException::class)
    fun handleTssException(tssException: TssException): ResponseEntity<Ressurs<Nothing>> {
        logger.warn("Feil mot TSS: ${tssException.message}", tssException)
        return when (tssException) {
            is TssConnectionException -> serviceUnavailable(tssException.message!!, tssException)
            is TssNoDataFoundException -> {
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Ressurs.failure(errorMessage = tssException.message))
            }
            else -> illegalState(tssException.message!!, tssException)
        }
    }

    @ExceptionHandler(FinnesIkkeITps::class)
    fun handleThrowable(feil: FinnesIkkeITps): ResponseEntity<Ressurs<Nothing>> {
        logger.warn("Personen finnes ikke i TPS system=${feil.system} ")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Ressurs.failure(errorMessage = "Personen finnes ikke i TPS"))
    }
}
