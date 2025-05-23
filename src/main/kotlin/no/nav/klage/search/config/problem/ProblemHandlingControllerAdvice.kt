package no.nav.klage.search.config.problem

import no.nav.klage.search.exceptions.*
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getTeamLogger
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ProblemHandlingControllerAdvice : ResponseEntityExceptionHandler() {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val ourLogger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    @ExceptionHandler
    fun handleMissingTilgang(ex: MissingTilgangException, request: NativeWebRequest): ProblemDetail =
        create(HttpStatus.FORBIDDEN, ex)

    @ExceptionHandler
    fun handleNotMatchingUser(ex: NotMatchingUserException, request: NativeWebRequest): ProblemDetail =
        create(HttpStatus.FORBIDDEN, ex)

    @ExceptionHandler
    fun handleFeatureNotEnabled(ex: FeatureNotEnabledException, request: NativeWebRequest): ProblemDetail =
        create(HttpStatus.FORBIDDEN, ex)

    @ExceptionHandler
    fun handleNoSaksbehandlerRoleEnabled(
        ex: NoSaksbehandlerRoleException,
        request: NativeWebRequest
    ): ProblemDetail =
        create(HttpStatus.FORBIDDEN, ex)

    @ExceptionHandler
    fun handleNotOwnEnhet(ex: NotOwnEnhetException, request: NativeWebRequest): ProblemDetail =
        create(HttpStatus.FORBIDDEN, ex)

    @ExceptionHandler
    fun handlePersonNotFound(ex: PersonNotFoundException, request: NativeWebRequest): ProblemDetail =
        create(HttpStatus.NOT_FOUND, ex)

    @ExceptionHandler
    fun handleEnhetNotFoundForSaksbehandlerException(
        ex: EnhetNotFoundForSaksbehandlerException,
        request: NativeWebRequest
    ): ProblemDetail =
        create(HttpStatus.INTERNAL_SERVER_ERROR, ex)

    @ExceptionHandler
    fun handleResponseStatusException(
        ex: WebClientResponseException,
        request: NativeWebRequest
    ): ProblemDetail =
        createProblemForWebClientResponseException(ex)

    private fun createProblemForWebClientResponseException(ex: WebClientResponseException): ProblemDetail {
        logError(
            httpStatus = HttpStatus.valueOf(ex.statusCode.value()),
            errorMessage = ex.statusText,
            exception = ex
        )

        return ProblemDetail.forStatus(ex.statusCode).apply {
            title = ex.statusText
            detail = ex.responseBodyAsString
        }
    }

    private fun create(httpStatus: HttpStatus, ex: Exception): ProblemDetail {
        val errorMessage = ex.message ?: "No error message available"

        logError(
            httpStatus = httpStatus,
            errorMessage = errorMessage,
            exception = ex
        )

        return ProblemDetail.forStatusAndDetail(httpStatus, errorMessage).apply {
            title = errorMessage
        }
    }

    private fun logError(httpStatus: HttpStatus, errorMessage: String, exception: Exception) {
        when {
            httpStatus.is5xxServerError -> {
                ourLogger.error("Exception thrown to client: ${exception.javaClass.name}. See more in team-logs.")
                teamLogger.error("Exception thrown to client: ${httpStatus.reasonPhrase}, $errorMessage", exception)
            }

            else -> {
                ourLogger.warn("Exception thrown to client: ${exception.javaClass.name}. See more in team-logs.")
                teamLogger.warn("Exception thrown to client: ${httpStatus.reasonPhrase}, $errorMessage", exception)
            }
        }
    }
}
