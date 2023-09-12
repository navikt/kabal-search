package no.nav.klage.search.domain

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import java.time.LocalDate

enum class SortField {
    FRIST, MOTTATT, PAA_VENT_FROM, PAA_VENT_TO, AVSLUTTET_AV_SAKSBEHANDLER, RETURNERT_FRA_ROL,
}

enum class Order {
    ASC, DESC
}

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

    override val sortField: SortField,
    override val order: Order,
) : PageableSearchCriteria, SecuritySearchCriteria, SortableSearchCriteria

data class FerdigstilteOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val navIdent: String,
    val ferdigstiltFom: LocalDate,
    val ferdigstiltTom: LocalDate,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class ReturnerteROLOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val navIdent: String,
    val returnertFom: LocalDate,
    val returnertTom: LocalDate,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class UferdigeOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val navIdent: String,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class OppgaverPaaVentSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val navIdent: String,

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
    val ferdigstiltTom: LocalDate,

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

data class BehandlingIdSearchCriteria(
    val behandlingId: String,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : SecuritySearchCriteria

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