package no.nav.klage.search.clients.azure

import no.nav.klage.search.config.CacheWithJCacheConfiguration
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class MicrosoftGraphClient(
    private val microsoftGraphWebClient: WebClient,
    private val tokenUtil: TokenUtil,
    @Value("\${KABAL_SAKSBEHANDLING_ROLE_ID}") private val kabalSaksbehandlingRoleId: String,
    @Value("\${KABAL_ROL_ROLE_ID}") private val kabalROLRoleId: String,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Retryable
    fun getEnhetensAnsatteWithKabalSaksbehandlerRole(enhetsnummer: String): AzureSlimUserList {
        logger.debug("getEnhetensAnsattesNavIdentsWithKabalSaksbehandlerRole from Microsoft Graph")
        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/groups/$kabalSaksbehandlingRoleId/transitivemembers/microsoft.graph.user")
                    .queryParam("\$filter", "streetAddress eq '$enhetsnummer'")
                    .queryParam("\$count", true)
                    .queryParam("\$top", 500)
                    .queryParam("\$select", "userPrincipalName,onPremisesSamAccountName,displayName")
                    .build()
            }
            .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithGraphScope()}")
            .header("ConsistencyLevel", "eventual")
            .retrieve()
            .bodyToMono<AzureSlimUserList>()
            .block()
            ?: throw RuntimeException("AzureAD data about authenticated user could not be fetched")
    }

    @Retryable
    fun getAnsatteWithKabalROLRole(): AzureSlimUserList {
        logger.debug("getEnhetensAnsatteWithKabalROLRole from Microsoft Graph")
        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/groups/$kabalROLRoleId/transitivemembers/microsoft.graph.user")
                    .queryParam("\$count", true)
                    .queryParam("\$top", 500)
                    .queryParam("\$select", "userPrincipalName,onPremisesSamAccountName,displayName")
                    .build()
            }
            .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithGraphScope()}")
            .header("ConsistencyLevel", "eventual")
            .retrieve()
            .bodyToMono<AzureSlimUserList>()
            .block()
            ?: throw RuntimeException("AzureAD data about authenticated user could not be fetched")
    }
}