package no.nav.klage.search.domain

import no.nav.klage.kodeverk.FlowState
import no.nav.klage.kodeverk.SattPaaVentReason
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import java.time.LocalDate

enum class SortField {
    FRIST, MOTTATT, PAA_VENT_FROM, PAA_VENT_TO, AVSLUTTET_AV_SAKSBEHANDLER, RETURNERT_FRA_ROL, VARSLET_FRIST
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
    val fristFrom: LocalDate
    val fristTo: LocalDate
    val varsletFristFrom: LocalDate
    val varsletFristTo: LocalDate
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

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

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

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

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

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,

    val muFlowStates: List<FlowState>,
    val rolFlowStates: List<FlowState>,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class OppgaverPaaVentSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val navIdent: String,
    val sattPaaVentReasons: List<SattPaaVentReason>,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

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

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class KrolsReturnerteOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val returnertFom: LocalDate,
    val returnertTom: LocalDate,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

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
    val medunderskrivere: List<String>,

    val sattPaaVentReasons: List<SattPaaVentReason>,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

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
    val medunderskrivere: List<String>,

    val muFlowStates: List<FlowState>,
    val rolFlowStates: List<FlowState>,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class KrolsUferdigeOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    val rolList: List<String>,

    override val sortField: SortField,
    override val order: Order,
    override val offset: Int,
    override val limit: Int,

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

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

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria

data class CountLedigeOppgaverMedUtgaattFristSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val fristFrom: LocalDate,
    override val fristTo: LocalDate,

    override val varsletFristFrom: LocalDate,
    override val varsletFristTo: LocalDate,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, SecuritySearchCriteria