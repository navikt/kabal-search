package no.nav.klage.search.clients.pdl.graphql

data class PersonGraphqlQuery(
    val query: String,
    val variables: FnrVariables
)

data class PersonerGraphqlQuery(
    val query: String,
    val variables: FnrListeVariables
)

data class FnrListeVariables(
    val identer: List<String>
)

data class FnrVariables(
    val ident: String
)

fun hentPersonerQuery(fnrList: List<String>): PersonerGraphqlQuery {
    val query =
        PersonGraphqlQuery::class.java.getResource("/pdl/hentPersonBolk.graphql").cleanForGraphql()
    return PersonerGraphqlQuery(query, FnrListeVariables(fnrList))
}

fun hentPersonQuery(fnr: String): PersonGraphqlQuery {
    val query =
        PersonGraphqlQuery::class.java.getResource("/pdl/hentPerson.graphql").cleanForGraphql()
    return PersonGraphqlQuery(query, FnrVariables(fnr))
}
