package no.nav.klage.search.clients.azure

import no.nav.klage.search.gateway.AzureGateway
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service

@Service
class DefaultAzureGateway(
    private val microsoftGraphClient: MicrosoftGraphClient,
) : AzureGateway {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    override fun getEnhetensAnsattesNavIdentsWithKabalSaksbehandlerRole(enhetsnummer: String): AzureSlimUserList =
        try {
            microsoftGraphClient.getEnhetensAnsatteWithKabalSaksbehandlerRole(enhetsnummer = enhetsnummer)
        } catch (e: Exception) {
            logger.error("Failed to call getEnhetensAnsattesNavIdents", e)
            throw e
        }

    override fun getAnsattesNavIdentsWithKabalROLRole(): AzureSlimUserList =
        try {
            microsoftGraphClient.getAnsatteWithKabalROLRole()
        } catch (e: Exception) {
            logger.error("Failed to call getEnhetensAnsattesNavIdents", e)
            throw e
        }
}