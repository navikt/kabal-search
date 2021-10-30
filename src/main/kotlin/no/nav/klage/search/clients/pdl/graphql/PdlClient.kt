package no.nav.klage.search.clients.pdl.graphql

import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.lang.System.currentTimeMillis

@Component
class PdlClient(
    private val pdlWebClient: WebClient,
    private val tokenUtil: TokenUtil
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Retryable
    fun getPersonerInfo(fnrList: List<String>): HentPersonerResponse {
        return runWithTiming {
            val stsSystembrukerToken = tokenUtil.getAppAccessTokenWithPdlScope()
            pdlWebClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer $stsSystembrukerToken")
                .bodyValue(hentPersonerQuery(fnrList))
                .retrieve()
                .bodyToMono<HentPersonerResponse>()
                .block() ?: throw RuntimeException("Person not found")
        }
    }

    fun <T> runWithTiming(block: () -> T): T {
        val start = currentTimeMillis()
        try {
            return block.invoke()
        } finally {
            val end = currentTimeMillis()
            logger.debug("Time it took to call pdl: ${end - start} millis")
        }
    }

    @Retryable
    fun getPersonInfo(fnr: String): HentPersonResponse {
        return runWithTiming {
            val stsSystembrukerToken = tokenUtil.getAppAccessTokenWithPdlScope()
            pdlWebClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer $stsSystembrukerToken")
                .bodyValue(hentPersonQuery(fnr))
                .retrieve()
                .bodyToMono<HentPersonResponse>()
                .block() ?: throw RuntimeException("Person not found")
        }
    }

    @Retryable
    fun personsok(inputString: String): SoekPersonResponse {
        return runWithTiming {
            val userToken = tokenUtil.getSaksbehandlerAccessTokenWithPdlScope()
            pdlWebClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
                .bodyValue(soekPersonNavnContainsQuery(inputString))
                .retrieve()
                .bodyToMono<SoekPersonResponse>()
                .block() ?: throw RuntimeException("Personsøk failed")
        }
    }
}
