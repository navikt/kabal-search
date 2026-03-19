package no.nav.klage.search.domain

import no.nav.klage.kodeverk.SattPaaVentReason
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import java.time.LocalDate

enum class SortField {
    FRIST, MOTTATT, PAA_VENT_FROM, PAA_VENT_TO, AVSLUTTET_AV_SAKSBEHANDLER, RETURNERT_FRA_ROL, VARSLET_FRIST
}

enum class HelperStatus {
    SENDT_TIL_MU,
    RETURNERT_FRA_MU,
    SENDT_TIL_FELLES_ROL_KOE,
    SENDT_TIL_ROL,
    RETURNERT_FRA_ROL,
    MU,
    ROL,
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

interface HelperStatusSearchCriteria {
    val helperStatusList: List<HelperStatus>
}

interface SattPaaVentSearchCriteria {
    val sattPaaVentReasons: List<SattPaaVentReason>
}

interface EnhetSearchCriteria {
    val enhetId: String
}

interface SaksbehandlereSearchCriteria {
    val saksbehandlere: List<String>
}

interface MedunderskrivereSearchCriteria {
    val medunderskrivere: List<String>
}

interface FerdigstiltSearchCriteria {
    val ferdigstiltFom: LocalDate
    val ferdigstiltTom: LocalDate
}

interface ReturnertSearchCriteria {
    val returnertFom: LocalDate
    val returnertTom: LocalDate
}

interface NavIdentSearchCriteria {
    val navIdent: String
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

data class FerdigstilteOppgaverForAssignedSaksbehandlerSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val navIdent: String,
    override val ferdigstiltFom: LocalDate,
    override val ferdigstiltTom: LocalDate,

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
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria,
    FerdigstiltSearchCriteria, NavIdentSearchCriteria

data class ReturnerteROLOppgaverForAssignedRolSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val navIdent: String,
    override val returnertFom: LocalDate,
    override val returnertTom: LocalDate,

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
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria, ReturnertSearchCriteria, NavIdentSearchCriteria

data class UferdigeOppgaverForSaksbehandlerOrMedunderskriverOrRolSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val navIdent: String,

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

    override val helperStatusList: List<HelperStatus>,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria,
    HelperStatusSearchCriteria, NavIdentSearchCriteria

data class OppgaverPaaVentForSaksbehandlerOrMedunderskriverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val navIdent: String,
    override val sattPaaVentReasons: List<SattPaaVentReason>,

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
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria,
    SattPaaVentSearchCriteria, NavIdentSearchCriteria

data class EnhetensFerdigstilteOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val enhetId: String,
    override val saksbehandlere: List<String>,
    override val ferdigstiltFom: LocalDate,
    override val ferdigstiltTom: LocalDate,

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
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria, EnhetSearchCriteria,
    SaksbehandlereSearchCriteria, FerdigstiltSearchCriteria

data class KrolsReturnerteOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val returnertFom: LocalDate,
    override val returnertTom: LocalDate,

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
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria, ReturnertSearchCriteria

data class EnhetensOppgaverPaaVentSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val enhetId: String,
    override val saksbehandlere: List<String>,
    override val medunderskrivere: List<String>,

    override val sattPaaVentReasons: List<SattPaaVentReason>,

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
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria,
    SattPaaVentSearchCriteria, EnhetSearchCriteria, SaksbehandlereSearchCriteria,
    MedunderskrivereSearchCriteria

data class EnhetensUferdigeOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val enhetId: String,
    override val saksbehandlere: List<String>,
    override val medunderskrivere: List<String>,

    override val helperStatusList: List<HelperStatus>,

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
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria,
    HelperStatusSearchCriteria, EnhetSearchCriteria, SaksbehandlereSearchCriteria,
    MedunderskrivereSearchCriteria

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


data class TildelteOppgaverSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val saksbehandlere: List<String>,
    override val medunderskrivere: List<String>,

    override val helperStatusList: List<HelperStatus>,

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
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria, SaksbehandlereSearchCriteria, MedunderskrivereSearchCriteria, HelperStatusSearchCriteria

data class OppgaverPaaVentSearchCriteria(
    override val typer: List<Type>,
    override val ytelser: List<Ytelse>,
    override val hjemler: List<Hjemmel>,

    override val saksbehandlere: List<String>,
    override val medunderskrivere: List<String>,

    override val sattPaaVentReasons: List<SattPaaVentReason>,

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
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria, SaksbehandlereSearchCriteria, MedunderskrivereSearchCriteria, SattPaaVentSearchCriteria