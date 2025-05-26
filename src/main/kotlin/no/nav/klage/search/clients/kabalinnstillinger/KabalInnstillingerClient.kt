package no.nav.klage.search.clients.kabalinnstillinger

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

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
        return kabalInnstillingerWebClient.get()
            .uri { it.path("/me/innstillinger").build() }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getUserAccessTokenWithKabalInnstillingerScope()}"
            )
            .retrieve()
            .bodyToMono<InnstillingerView>()
            .block() ?: throw RuntimeException("Could not get innstillinger for current saksbehandler")
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class InnstillingerView(
    val hjemler: List<String>,
    val ytelser: List<String>,
)