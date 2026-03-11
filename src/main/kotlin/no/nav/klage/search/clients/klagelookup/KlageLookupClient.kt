package no.nav.klage.search.clients.klagelookup

import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.search.exceptions.UserNotFoundException
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.logErrorResponse
import org.springframework.http.HttpHeaders
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono


@Component
class KlageLookupClient(
    private val klageLookupWebClient: WebClient,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Retryable(
        excludes = [UserNotFoundException::class]
    )
    fun getUserInfo(
        navIdent: String,
    ): ExtendedUserResponse {
        return runWithTimingAndLogging {
            val token = getCorrectBearerToken()
            klageLookupWebClient.get()
                .uri("/users/$navIdent")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    token,
                )
                .exchangeToMono { response ->
                    if (response.statusCode().value() == 404) {
                        logger.debug("User $navIdent not found")
                        Mono.error(UserNotFoundException("User $navIdent not found"))
                    } else if (response.statusCode().isError) {
                        logErrorResponse(
                            response = response,
                            functionName = ::getUserInfo.name,
                            classLogger = logger,
                        )
                        response.createError()
                    } else {
                        response.bodyToMono<ExtendedUserResponse>()
                    }
                }
                .block() ?: throw RuntimeException("Could not get user info for $navIdent")
        }
    }

    @Retryable(
        excludes = [UserNotFoundException::class]
    )
    fun getUserGroups(
        navIdent: String,
    ): SaksbehandlerGroups {
        return runWithTimingAndLogging {
            val token = getCorrectBearerToken()
            klageLookupWebClient.get()
                .uri("/users/$navIdent/groups")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    token,
                )
                .exchangeToMono { response ->
                    if (response.statusCode().value() == 404) {
                        logger.debug("User $navIdent not found")
                        Mono.error(UserNotFoundException("User $navIdent not found"))
                    } else if (response.statusCode().isError) {
                        logErrorResponse(
                            response = response,
                            functionName = ::getUserGroups.name,
                            classLogger = logger,
                        )
                        response.createError()
                    } else {
                        response.bodyToMono<GroupsResponse>()
                    }
                }
                .block() ?: throw RuntimeException("Could not get user groups for navIdent $navIdent")
        }.toSaksbehandlerGroups()
    }

    fun <T> runWithTimingAndLogging(block: () -> T): T {
        val start = System.currentTimeMillis()
        try {
            return block.invoke()
        } finally {
            val end = System.currentTimeMillis()
            logger.debug("Time it took to call klage-lookup: ${end - start} millis")
        }
    }

    private fun getCorrectBearerToken(): String {
        return when (tokenUtil.getCurrentTokenType()) {
            TokenUtil.TokenType.OBO -> "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithKlageLookupScope()}"
            TokenUtil.TokenType.CC, TokenUtil.TokenType.UNAUTHENTICATED -> "Bearer ${tokenUtil.getAppAccessTokenWithKlageLookupScope()}"
        }
    }

    fun GroupsResponse.toSaksbehandlerGroups(): SaksbehandlerGroups {
        return SaksbehandlerGroups(
            groups = this.groupIds.map { AzureGroup.of(it) }
        )
    }
}