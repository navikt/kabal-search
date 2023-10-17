package no.nav.klage.search.clients.azure

import no.nav.klage.search.config.CacheWithJCacheConfiguration
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.Retryable
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
        private val secureLogger = getSecureLogger()

        private const val userSelect =
            "onPremisesSamAccountName,displayName,givenName,surname,mail,officeLocation,userPrincipalName,id,jobTitle,streetAddress"

        private const val slimUserSelect = "userPrincipalName,onPremisesSamAccountName,displayName"
    }

    //TODO: navIdent er bare så cachen skal ha en nøkkel. Det er mulig å dra nøkkelen ut av responsen også tror jeg, men det får vi bruke tid på en annen gang..
    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.AZUREUSER_CACHE)
    fun getInnloggetSaksbehandler(navIdent: String): AzureUser {
        logger.debug("Fetching data about authenticated user from Microsoft Graph")
        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/me")
                    .queryParam("\$select", userSelect)
                    .build()
            }.header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")

            .retrieve()
            .bodyToMono<AzureUser>()
            .block().let { secureLogger.debug("me: $it"); it }
            ?: throw RuntimeException("AzureAD data about authenticated user could not be fetched")
    }

    @Retryable
    fun getAllDisplayNames(idents: List<List<String>>): Map<String, String> {
        val queryString = idents.map {
            it.joinToString(separator = "','", prefix = "('", postfix = "')")
        }

        val data = Flux.fromIterable(queryString)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap {
                getDisplayNames(it)
            }
            .ordered { _: AzureSlimUserList, _: AzureSlimUserList -> 1 }.toIterable()

        return data.flatMap {
            it.value ?: emptyList()
        }.associate {
            secureLogger.debug("Display name: {}", it)
            it.onPremisesSamAccountName to it.displayName
        }
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
    fun getEnhetensAnsatteWithKabalROLRole(enhetsnummer: String): AzureSlimUserList {
        logger.debug("getEnhetensAnsatteWithKabalROLRole from Microsoft Graph")
        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/groups/$kabalROLRoleId/transitivemembers/microsoft.graph.user")
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


    private fun getDisplayNames(navIdents: String): Mono<AzureSlimUserList> {
        return try {
            microsoftGraphWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/users")
                        .queryParam("\$filter", "mailnickname in $navIdents")
                        .queryParam("\$select", slimUserSelect)
                        .build()
                }.header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithGraphScope()}")
                .retrieve()
                .bodyToMono()
        } catch (e: Exception) {
            logger.warn("Could not fetch displayname for idents: $navIdents", e)
            Mono.empty()
        }
    }

    @Retryable
    fun getEnhetsnummerForNavIdent(navIdent: String): String {
        logger.debug("findEnhetsnummerForNavIdent $navIdent")
        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/users")
                    .queryParam("\$filter", "onPremisesSamAccountName eq '$navIdent'")
                    .queryParam("\$select", userSelect)
                    .queryParam("\$count", true)
                    .build()
            }
            .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithGraphScope()}")
            .header("ConsistencyLevel", "eventual")
            .retrieve()
            .bodyToMono<AzureUserList>().block()?.value?.firstOrNull()?.streetAddress
            ?.let { secureLogger.debug("NavIdent enhet: {}", it); it }
            ?: throw RuntimeException("AzureAD data about user by navIdent could not be fetched")
    }
}