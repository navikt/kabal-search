package no.nav.klage.search.api.view

data class BehandlingerListResponse(
    val antallTreffTotalt: Int,
    val behandlinger: List<String>
)