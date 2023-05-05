package no.nav.klage.search.api.view

data class BehandlingerListResponsNew(
    val antallTreffTotalt: Int,
    val behandlinger: List<BehandlingListViewNew>
)

data class BehandlingListViewNew(
    val id: String,
)