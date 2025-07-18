package no.nav.klage.search.api.view

/**
 * When searching for a specific fnr
 */
data class FnrSearchResponseWithoutPerson(
    val aapneBehandlinger: List<String>,
    val avsluttedeBehandlinger: List<String>,
    val feilregistrerteBehandlinger: List<String>,
    val paaVentBehandlinger: List<String>,
)