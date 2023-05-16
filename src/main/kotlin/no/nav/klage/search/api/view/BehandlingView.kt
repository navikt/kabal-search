package no.nav.klage.search.api.view

import no.nav.klage.kodeverk.MedunderskriverFlyt
import java.time.LocalDate
import java.time.LocalDateTime

data class BehandlingerListResponse(
    val antallTreffTotalt: Int,
    val behandlinger: List<BehandlingListView>
)

data class BehandlingListView(
    val id: String,
)

data class PersonView(
    val fnr: String,
    val navn: String,
)

enum class AccessView {
    NONE, READ, ASSIGN, WRITE
}

data class BehandlingView(
    val id: String,
    val type: String,
    val ytelse: String,
    val tema: String,
    val hjemmel: String?,
    val frist: LocalDate?,
    val mottatt: LocalDate,
    val erMedunderskriver: Boolean = false,
    val harMedunderskriver: Boolean = false,
    val medunderskriverident: String?,
    val medunderskriverNavn: String?,
    val medunderskriverFlyt: MedunderskriverFlyt,
    val utfall: String?,
    val avsluttetAvSaksbehandlerDate: LocalDate?,
    val isAvsluttetAvSaksbehandler: Boolean,
    val erTildelt: Boolean,
    val tildeltSaksbehandlerident: String?,
    val tildeltSaksbehandlerNavn: String?,
    val saksbehandlerHarTilgang: Boolean,
    val egenAnsatt: Boolean,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val ageKA: Int,
    val access: AccessView,
    val sattPaaVent: Venteperiode?,
    val feilregistrert: LocalDateTime?,
)

data class Venteperiode(
    val from: LocalDate?,
    val to: LocalDate?,
    val isExpired: Boolean?,
)