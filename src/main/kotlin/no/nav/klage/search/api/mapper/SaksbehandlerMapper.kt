package no.nav.klage.search.api.mapper

import no.nav.klage.search.api.view.SaksbehandlerView
import no.nav.klage.search.domain.kodeverk.Hjemmel
import no.nav.klage.search.domain.kodeverk.Tema
import no.nav.klage.search.domain.kodeverk.Type
import no.nav.klage.search.domain.saksbehandler.SaksbehandlerInnstillinger


fun SaksbehandlerView.InnstillingerView.mapToDomain() = SaksbehandlerInnstillinger(
    hjemler = hjemler.map { Hjemmel.of(it) },
    temaer = temaer.map { Tema.of(it) },
    typer = typer.map { Type.of(it) }
)

