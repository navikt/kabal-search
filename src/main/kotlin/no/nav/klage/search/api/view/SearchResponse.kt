package no.nav.klage.search.api.view

/**
 * When searching for a specific fnr
 */
data class FnrSearchResponse(
    val fnr: String,
    val navn: NavnView,
    val behandlinger: List<BehandlingListView> = emptyList(),
    val aapneBehandlinger: List<BehandlingListView> = emptyList(),
    val avsluttedeBehandlinger: List<BehandlingListView> = emptyList()
)

/**
 * Used for name search
 */
data class NameSearchResponse(
    val people: List<PersonView>
) {
    data class PersonView(
        val fnr: String,
        val navn: NavnView
    )
}

data class NavnView(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
)
