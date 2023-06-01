package no.nav.klage.search.api.view

/**
 * When searching for a specific fnr
 */
data class FnrSearchResponseWithoutPerson(
    val aapneBehandlinger: List<String>,
    val avsluttedeBehandlinger: List<String>,
    val feilregistrerteBehandlinger: List<String>,
)

/**
 * Used for name search
 */
data class NameSearchResponse(
    val people: List<PersonView>
) {
    data class PersonView(
        val id: String,
        val name: String,
    )
}