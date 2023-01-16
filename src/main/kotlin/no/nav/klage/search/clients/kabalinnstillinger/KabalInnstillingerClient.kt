package no.nav.klage.search.clients.kabalinnstillinger

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDateTime
import java.util.*

@Component
class KabalInnstillingerClient(
    private val kabalInnstillingerWebClient: WebClient,
    private val tokenUtil: TokenUtil,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun getSaksbehandlersTildelteYtelser(navIdent: String): SaksbehandlerAccess {
        logger.debug("Getting tildelte ytelser for $navIdent in kabal-innstillinger")
        return kabalInnstillingerWebClient.get()
            .uri { it.path("/ansatte/$navIdent/tildelteytelser").build() }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getUserAccessTokenWithKabalInnstillingerScope()}"
            )
            .retrieve()
            .bodyToMono<SaksbehandlerAccess>()
            .block() ?: throw RuntimeException("Could not get tildelte ytelser")
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SaksbehandlerAccess(
    val saksbehandlerIdent: String,
    val saksbehandlerName: String,
    val ytelseIdList: List<String>,
    val created: LocalDateTime?,
    val accessRightsModified: LocalDateTime?,
)