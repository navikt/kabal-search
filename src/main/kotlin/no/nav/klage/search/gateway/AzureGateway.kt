package no.nav.klage.search.gateway

import no.nav.klage.search.clients.azure.AzureSlimUserList

interface AzureGateway {
    fun getEnhetensAnsattesNavIdentsWithKabalSaksbehandlerRole(enhetsnummer: String): AzureSlimUserList
    fun getAnsattesNavIdentsWithKabalROLRole(): AzureSlimUserList
}