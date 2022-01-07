package no.nav.klage.search.domain.saksbehandler

data class SaksbehandlerPersonligInfo(
    val navIdent: String,
    val azureId: String,
    val fornavn: String,
    val etternavn: String,
    val sammensattNavn: String,
    val epost: String,
    val enhet: Enhet,
)