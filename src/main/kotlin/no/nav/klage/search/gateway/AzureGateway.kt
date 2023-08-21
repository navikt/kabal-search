package no.nav.klage.search.gateway

import no.nav.klage.search.domain.saksbehandler.SaksbehandlerPersonligInfo

interface AzureGateway {
    fun getDataOmInnloggetSaksbehandler(): SaksbehandlerPersonligInfo
    fun getAllDisplayNames(idents: List<List<String>>): Map<String, String>
    fun getEnhetsnummerForNavIdent(ident: String): String
}