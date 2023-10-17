package no.nav.klage.search.api.view

data class SaksbehandlereListResponse(
    val saksbehandlere: List<SaksbehandlerView>
)

data class MedunderskrivereListResponse(
    val medunderskrivere: List<SaksbehandlerView>
)

data class ROLListResponse(
    val rolList: List<SaksbehandlerView>
)

data class SaksbehandlerView(val navIdent: String, val navn: String)
