package no.nav.klage.search.api.view

import no.nav.klage.kodeverk.MedunderskriverFlyt
import java.time.LocalDate
import java.time.LocalDateTime

data class BehandlingerListResponse(
    val antallTreffTotalt: Int,
    val behandlinger: List<String>
)

enum class AccessView {
    NONE, READ, ASSIGN, WRITE
}

data class BehandlingView(
    val id: String,
    val type: String,
    val typeId: String,
    val ytelse: String,
    val ytelseId: String,
    val tema: String,
    val hjemmel: String?,
    val hjemmelId: String?,
    val frist: LocalDate?,
    val mottatt: LocalDate,
    val erMedunderskriver: Boolean = false,
    val harMedunderskriver: Boolean = false,
    val medunderskriverident: String?,
    val medunderskriverNavn: String?,
    val medunderskriverFlyt: MedunderskriverFlyt,
    val medunderskriverFlytId: String,
    val utfall: String?,
    val utfallId: String?,
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