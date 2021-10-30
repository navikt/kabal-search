package no.nav.klage.search.api.view

data class SearchPersonByNameInput(
    val query: String,
    val start: Int,
    val antall: Int
)

data class SearchPersonByFnrInput(
    val query: String,
    val enhet: String
)

