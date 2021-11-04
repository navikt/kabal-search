package no.nav.klage.search.clients.azure

import no.nav.klage.search.gateway.AzureGateway
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class DefaultAzureGateway(private val microsoftGraphClient: MicrosoftGraphClient) : AzureGateway {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    override fun getAllDisplayNames(idents: List<List<String>>): Map<String, String> =
        try {
            microsoftGraphClient.getAllDisplayNames(idents)
        } catch (e: Exception) {
            logger.error("Failed to call getAllDisplayNames", e)
            throw e
        }

}