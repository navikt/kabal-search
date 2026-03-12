package no.nav.klage.search.service.saksbehandler

import no.nav.klage.search.api.view.SaksbehandlerView
import no.nav.klage.search.clients.klagelookup.KlageLookupClient
import no.nav.klage.search.gateway.AzureGateway
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val azureGateway: AzureGateway,
    private val klageLookupClient: KlageLookupClient,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getNameForIdent(navIdent: String): String? {
        return klageLookupClient.getUserInfo(navIdent = navIdent).sammensattNavn
    }

    fun getSaksbehandlereForEnhet(enhetsnummer: String): List<SaksbehandlerView> {
        val azureOutput =
            azureGateway.getEnhetensAnsattesNavIdentsWithKabalSaksbehandlerRole(enhetsnummer = enhetsnummer)
        return azureOutput.value?.map {
            SaksbehandlerView(
                navIdent = it.onPremisesSamAccountName,
                navn = it.displayName,
            )
        } ?: emptyList()
    }

    fun getROLList(): List<SaksbehandlerView> {
        val azureOutput = azureGateway.getAnsattesNavIdentsWithKabalROLRole()
        return azureOutput.value?.map {
            SaksbehandlerView(
                navIdent = it.onPremisesSamAccountName,
                navn = it.displayName,
            )
        } ?: emptyList()
    }

    fun getEnhetsnummerForNavIdent(navIdent: String): String? =
        klageLookupClient.getUserInfo(navIdent = navIdent).enhet.enhetNr
}
