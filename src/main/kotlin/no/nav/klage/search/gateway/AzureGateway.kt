package no.nav.klage.search.gateway

import no.nav.klage.search.clients.azure.AzureSlimUserList
import no.nav.klage.search.domain.saksbehandler.SaksbehandlerPersonligInfo

interface AzureGateway {
    fun getDataOmInnloggetSaksbehandler(): SaksbehandlerPersonligInfo
    fun getAllDisplayNames(idents: List<List<String>>): Map<String, String>
    fun getEnhetensAnsattesNavIdentsWithKabalSaksbehandlerRole(enhetsnummer: String): AzureSlimUserList
}