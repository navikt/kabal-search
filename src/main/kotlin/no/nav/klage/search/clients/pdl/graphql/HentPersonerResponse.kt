package no.nav.klage.search.clients.pdl.graphql

data class HentPersonResponse(val data: DataWrapper?, val errors: List<PdlError>? = null)

data class DataWrapper(val hentPerson: PdlPerson?)

data class PdlPerson(
    val adressebeskyttelse: List<Adressebeskyttelse>,
) {
    data class Adressebeskyttelse(val gradering: GraderingType) {
        enum class GraderingType { STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG, FORTROLIG, UGRADERT }
    }
}
