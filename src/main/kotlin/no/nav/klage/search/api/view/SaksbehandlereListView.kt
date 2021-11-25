package no.nav.klage.search.api.view

data class SaksbehandlereListResponse(
    val saksbehandlere: List<SaksbehandlerView>
) {
    data class SaksbehandlerView(val navIdent: String, val navn: String)
}