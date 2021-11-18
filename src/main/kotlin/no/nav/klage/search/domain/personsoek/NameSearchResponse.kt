package no.nav.klage.search.domain.personsoek

data class Person(
    val fnr: String,
    val name: String,
    val navn: Navn,
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
)