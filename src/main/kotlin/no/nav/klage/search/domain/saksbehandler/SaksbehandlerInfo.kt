package no.nav.klage.search.domain.saksbehandler

data class SaksbehandlerInfo(
    val info: SaksbehandlerPersonligInfo,
    val roller: List<SaksbehandlerRolle>,
    val enheter: EnheterMedLovligeTemaer,
    val valgtEnhet: EnhetMedLovligeTemaer,
    val innstillinger: SaksbehandlerInnstillinger
)