package no.nav.klage.search.api.view

/**
 * When searching for a specific fnr
 */
data class FnrSearchResponse(
    val fnr: String,
    val navn: NavnView,
    val behandlinger: List<BehandlingView>,
    val aapneBehandlinger: List<BehandlingView>,
    val avsluttedeBehandlinger: List<BehandlingView>,
    val feilregistrerteBehandlinger: List<BehandlingView>,
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
