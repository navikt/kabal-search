package no.nav.klage.search.api.view

/**
 * When searching for a specific fnr
 */
data class FnrSearchResponse(
    val fnr: String?,
    val navn: NavnView?,
    val aapneBehandlinger: List<BehandlingListView>,
    val avsluttedeBehandlinger: List<BehandlingListView>,
    val feilregistrerteBehandlinger: List<BehandlingListView>,
)

/**
 * When searching for a specific fnr
 */
data class FnrSearchResponseWithoutPerson(
    val aapneBehandlinger: List<BehandlingListView>,
    val avsluttedeBehandlinger: List<BehandlingListView>,
    val feilregistrerteBehandlinger: List<BehandlingListView>,
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
