package no.nav.klage.search.api.view

import no.nav.klage.search.domain.kodeverk.MedunderskriverFlyt
import java.time.LocalDate

data class KlagebehandlingerListRespons(
    val antallTreffTotalt: Int,
    val klagebehandlinger: List<KlagebehandlingListView>
)

data class KlagebehandlingListView(
    val id: String,
    val person: Person? = null,
    val type: String,
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
    val ageKA: Int
) {
    data class Person(
        val fnr: String?,
        val navn: String?,
        val sivilstand: String? = null
    )
}

data class KlagebehandlingerPersonSoekListRespons(
    val antallTreffTotalt: Int,
    val personer: List<PersonSoekPersonView>
)

data class PersonSoekPersonView(
    val fnr: String,
    val navn: String?,
    val foedselsdato: LocalDate?,
    val klagebehandlinger: List<KlagebehandlingListView> = emptyList(),
    val aapneKlagebehandlinger: List<KlagebehandlingListView> = emptyList(),
    val avsluttedeKlagebehandlinger: List<KlagebehandlingListView> = emptyList()
)
