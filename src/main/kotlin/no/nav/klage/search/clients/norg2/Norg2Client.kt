package no.nav.klage.search.clients.norg2

import no.nav.klage.search.config.CacheWithJCacheConfiguration.Companion.ENHET_CACHE
import no.nav.klage.search.util.getLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
// Denne er ikke i bruk per nå, men jeg beholder den i tilfelle vi trenger enhetnavn i ES i tillegg til nr..
class Norg2Client(private val norg2WebClient: WebClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Retryable
    @Cacheable(ENHET_CACHE)
    fun fetchEnhet(enhetNr: String): Enhet {
        return try {
            norg2WebClient.get()
                .uri("/enhet/{enhetNr}", enhetNr)
                .retrieve()
                .bodyToMono<EnhetResponse>()
                .block()
                ?.asEnhet() ?: throw RuntimeException("No enhet returned for enhetNr $enhetNr")
        } catch (ex: Exception) {
            val errorMessage = "Problems with getting enhet $enhetNr from Norg2"
            logger.error(errorMessage, ex)
            throw RuntimeException(errorMessage, ex)
        }
    }

}
