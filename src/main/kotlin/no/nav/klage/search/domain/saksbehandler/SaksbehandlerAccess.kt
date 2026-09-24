package no.nav.klage.search.domain.saksbehandler

import no.nav.klage.kodeverk.ytelse.Ytelse
import java.time.LocalDateTime

data class SaksbehandlerAccess(
    val saksbehandlerIdent: String,
    val saksbehandlerName: String,
    val ytelser: List<Ytelse>,
    val anketeam: Boolean,
    val created: LocalDateTime?,
    val accessRightsModified: LocalDateTime?,
)
