package no.nav.klage.search.gateway

import no.nav.klage.search.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.search.domain.saksbehandler.SaksbehandlerRolle

interface AzureGateway {
    fun getRolleIder(ident: String): List<String>
    fun getGroupMembersNavIdents(groupid: String): List<String>
    fun getAllDisplayNames(idents: List<List<String>>): Map<String, String>
    fun getPersonligDataOmSaksbehandlerMedIdent(navIdent: String): SaksbehandlerPersonligInfo
    fun getDataOmInnloggetSaksbehandler(): SaksbehandlerPersonligInfo
    fun getRollerForSaksbehandlerMedIdent(navIdent: String): List<SaksbehandlerRolle>
    fun getRollerForInnloggetSaksbehandler(): List<SaksbehandlerRolle>
}