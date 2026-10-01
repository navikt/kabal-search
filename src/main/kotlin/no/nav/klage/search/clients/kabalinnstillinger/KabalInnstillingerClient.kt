package no.nav.klage.search.clients.kabalinnstillinger

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.logErrorResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDateTime

@Component
class KabalInnstillingerClient(
    private val kabalInnstillingerWebClient: WebClient,
    private val tokenUtil: TokenUtil,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getInnloggetSaksbehandlersInnstillinger(): InnstillingerView {
        logger.debug("Getting innstillinger for current saksbehandler in kabal-innstillinger")
        return kabalInnstillingerWebClient
            .get()
            .uri { it.path("/me/innstillinger").build() }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getUserAccessTokenWithKabalInnstillingerScope()}",
            ).retrieve()
            .bodyToMono<InnstillingerView>()
            .block() ?: throw RuntimeException("Could not get innstillinger for current saksbehandler")
    }

    fun getSaksbehandlersAccess(navIdent: String): SaksbehandlerAccessView {
        logger.debug("Getting access info for saksbehandler in kabal-innstillinger")
        return kabalInnstillingerWebClient
            .get()
            .uri { it.path("/ansatte/$navIdent/access").build() }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getUserAccessTokenWithKabalInnstillingerScope()}",
            ).retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                logErrorResponse(
                    response = response,
                    functionName = ::getSaksbehandlersAccess.name,
                    classLogger = logger,
                )
            }.bodyToMono<SaksbehandlerAccessView>()
            .block() ?: throw RuntimeException("Could not get access info for saksbehandler")
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class InnstillingerView(
    val hjemler: List<String>,
    val ytelser: List<String>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SaksbehandlerAccessView(
    val saksbehandlerIdent: String,
    val saksbehandlerName: String,
    val ytelseIdList: List<String>,
    val anketeam: Boolean,
    val created: LocalDateTime?,
    val accessRightsModified: LocalDateTime?,
)
