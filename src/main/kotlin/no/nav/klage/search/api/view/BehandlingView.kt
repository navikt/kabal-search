package no.nav.klage.search.api.view

import no.nav.klage.kodeverk.MedunderskriverFlyt
import java.time.LocalDate
import java.time.LocalDateTime

data class BehandlingerListResponse(
    val antallTreffTotalt: Int,
    val behandlinger: List<String>
)

data class BehandlingView(
    val id: String,
    val behandlingId: String,
    val type: String,
    val typeId: String,
    val ytelse: String,
    val ytelseId: String,
    val hjemmel: String?,
    val hjemmelId: String?,
    val frist: LocalDate?,
    val mottatt: LocalDate,
    val medunderskriverident: String?,
    val medunderskriverFlyt: MedunderskriverFlyt,
    val medunderskriverFlytId: String,
    val utfall: String?,
    val utfallId: String?,
    val avsluttetAvSaksbehandlerDate: LocalDate?,
    val isAvsluttetAvSaksbehandler: Boolean,
    val tildeltSaksbehandlerident: String?,
    val tildeltSaksbehandlerNavn: String?,
    val ageKA: Int,
    val sattPaaVent: SattPaaVent?,
    val feilregistrert: LocalDateTime?,
    val fagsystemId: String,
)

data class SattPaaVent(
    val from: LocalDate,
    val to: LocalDate,
    val isExpired: Boolean,
    val reason: String,
)