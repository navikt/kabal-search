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
    val typeId: String,
    val ytelseId: String,
    val hjemmelId: String?,
    val frist: LocalDate?,
    val mottatt: LocalDate,
    val medunderskriverident: String?,
    val medunderskriverFlyt: MedunderskriverFlyt,
    val medunderskriverFlytId: String,
    val utfallId: String?,
    val avsluttetAvSaksbehandlerDate: LocalDate?,
    val isAvsluttetAvSaksbehandler: Boolean,
    val tildeltSaksbehandlerident: String?,    
    val ageKA: Int,
    val sattPaaVent: SattPaaVent?,
    val feilregistrert: LocalDateTime?,
    val fagsystemId: String,
    val saksnummer: String,
)

data class SattPaaVent(
    val from: LocalDate,
    val to: LocalDate,
    val isExpired: Boolean,
    val reason: String,
)