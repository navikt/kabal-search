package no.nav.klage.search.api.view

data class SearchPersonByFnrInput(
    val query: String,
    val enhet: String?
)

