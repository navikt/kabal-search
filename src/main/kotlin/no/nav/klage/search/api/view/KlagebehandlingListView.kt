package no.nav.klage.search.api.view

import no.nav.klage.kodeverk.MedunderskriverFlyt
import java.time.LocalDate

data class KlagebehandlingerListRespons(
    val antallTreffTotalt: Int,
    val klagebehandlinger: List<KlagebehandlingListView>
)

data class PersonView(
    val fnr: String?,
    val navn: String?,
    val sivilstand: String? = null
)

enum class AccessView {
    NONE, READ, ASSIGN, WRITE
}

data class KlagebehandlingListView(
    val id: String,
    val person: PersonView? = null,
    val type: String,
    val ytelse: String?,
    val tema: String,
    val hjemmel: String?,
    val frist: LocalDate?,
    val mottatt: LocalDate?,
    val erMedunderskriver: Boolean = false,
    val harMedunderskriver: Boolean = false,
    val medunderskriverident: String?,
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
)
