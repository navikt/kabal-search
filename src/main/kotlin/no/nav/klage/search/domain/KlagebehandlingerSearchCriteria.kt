package no.nav.klage.search.domain

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import java.time.LocalDate

/*
data class KlagebehandlingerSearchCriteria(
    override val typer: List<Type> = emptyList(),
    override val ytelser: List<Ytelse> = emptyList(),
    override val hjemler: List<Hjemmel> = emptyList(),
    val statuskategori: Statuskategori = Statuskategori.AAPEN,

    val opprettetFom: LocalDateTime? = null,
    val opprettetTom: LocalDateTime? = null,
    val ferdigstiltFom: LocalDate? = null,
    val ferdigstiltTom: LocalDate? = null,
    val fristFom: LocalDate? = null,
    val fristTom: LocalDate? = null,
    val foedselsnr: String? = null,
    val extraPersonWithYtelser: ExtraPersonWithYtelser? = null,
    val raw: String = "",

    override val order: Order = Order.ASC,
    override val offset: Int,
    override val limit: Int,
    val erTildeltSaksbehandler: Boolean? = null,
    val saksbehandlere: List<String> = emptyList(),
    val enhetId: String? = null,
    override val sortField: SortField = SortField.FRIST,
    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria
*/

//data class ExtraPersonWithYtelser(val foedselsnr: String, val ytelser: List<Ytelse>)

enum class SortField {
    FRIST, MOTTATT, PAA_VENT_FROM, PAA_VENT_TO
}

enum class Order {
    ASC, DESC
}

//enum class Statuskategori {
//    AAPEN, AVSLUTTET, ALLE
//}

//fun KlagebehandlingerSearchCriteria.isFnrSoek() = raw.isNumeric()

//private fun String.isNumeric() = toLongOrNull() != null

interface PageableSearchCriteria {
    val offset: Int
    val limit: Int
}

interface BasicSearchCriteria {
    val typer: List<Type>
    val ytelser: List<Ytelse>
    val hjemler: List<Hjemmel>
}

interface SortableSearchCriteria {
    val order: Order
    val sortField: SortField
}

interface SecuritySearchCriteria {
    val kanBehandleEgenAnsatt: Boolean
    val kanBehandleFortrolig: Boolean
    val kanBehandleStrengtFortrolig: Boolean
}

data class OppgaverOmPersonSearchCriteria(
    val fnr: String,

    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : PageableSearchCriteria, SecuritySearchCriteria

data class SaksbehandlersFerdigstilteOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val saksbehandler: String,
    val ferdigstiltFom: LocalDate,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class SaksbehandlersUferdigeOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val saksbehandler: String,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class SaksbehandlersOppgaverPaaVentSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val saksbehandler: String,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class EnhetensFerdigstilteOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val enhetId: String,
    val saksbehandlere: List<String>,
    val ferdigstiltFom: LocalDate,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class EnhetensOppgaverPaaVentSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val enhetId: String,
    val saksbehandlere: List<String>,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class EnhetensUferdigeOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val enhetId: String,
    val saksbehandlere: List<String>,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class LedigeOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class CountLedigeOppgaverMedUtgaattFristSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val fristFom: LocalDate,
    val fristTom: LocalDate,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, SecuritySearchCriteria