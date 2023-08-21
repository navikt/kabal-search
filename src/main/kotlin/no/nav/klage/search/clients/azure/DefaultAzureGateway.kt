package no.nav.klage.search.clients.azure

import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.search.exceptions.EnhetNotFoundForSaksbehandlerException
import no.nav.klage.search.gateway.AzureGateway
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.stereotype.Service
import no.nav.klage.kodeverk.Enhet as KodeverkEnhet

@Service
class DefaultAzureGateway(
    private val microsoftGraphClient: MicrosoftGraphClient,
    private val tokenUtil: TokenUtil
) : AzureGateway {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    override fun getDataOmInnloggetSaksbehandler(): SaksbehandlerPersonligInfo {
        val data = try {
            microsoftGraphClient.getInnloggetSaksbehandler(tokenUtil.getIdent())
        } catch (e: Exception) {
            logger.error("Failed to call getInnloggetSaksbehandler", e)
            throw e
        }
        return SaksbehandlerPersonligInfo(
            data.onPremisesSamAccountName,
            data.id,
            data.givenName,
            data.surname,
            data.displayName,
            data.mail,
            mapToEnhet(data.streetAddress),
        )
    }

    override fun getAllDisplayNames(idents: List<List<String>>): Map<String, String> =
        try {
            microsoftGraphClient.getAllDisplayNames(idents)
        } catch (e: Exception) {
            logger.error("Failed to call getAllDisplayNames", e)
            throw e
        }

    override fun getEnhetsnummerForNavIdent(ident: String): String =
        try {
            microsoftGraphClient.getEnhetsnummerForNavIdent(ident)
        } catch (e: Exception) {
            logger.error("Failed to call getEnhetsnummerForNavIdent", e)
            throw e
        }

    private fun mapToEnhet(enhetNr: String): Enhet =
        KodeverkEnhet.values().find { it.navn == enhetNr }
            ?.let { Enhet(it.navn, it.beskrivelse) }
            ?: throw EnhetNotFoundForSaksbehandlerException("Enhet ikke funnet med enhetNr $enhetNr")

}