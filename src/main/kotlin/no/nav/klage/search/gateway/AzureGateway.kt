package no.nav.klage.search.gateway

interface AzureGateway {
    fun getAllDisplayNames(idents: List<List<String>>): Map<String, String>
}