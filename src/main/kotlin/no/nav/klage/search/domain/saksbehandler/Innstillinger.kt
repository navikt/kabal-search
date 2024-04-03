package no.nav.klage.search.domain.saksbehandler

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel

data class Innstillinger(
    val hjemler: List<Hjemmel>,
    val ytelser: List<Ytelse>,
)