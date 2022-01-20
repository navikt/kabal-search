package no.nav.klage.search.domain

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import java.time.LocalDate
import java.time.LocalDateTime

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

data class ExtraPersonWithYtelser(val foedselsnr: String, val ytelser: List<Ytelse>)

enum class SortField {
    FRIST, MOTTATT
}

enum class Order {
    ASC, DESC
}

enum class Statuskategori {
    AAPEN, AVSLUTTET, ALLE
}

fun KlagebehandlingerSearchCriteria.isFnrSoek() = raw.isNumeric()

private fun String.isNumeric() = toLongOrNull() != null

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

data class LedigeOppgaverSearchCriteria(
    override val typer: List<Type> = listOf(Type.KLAGE),
    override val ytelser: List<Ytelse> = emptyList(),
    override val hjemler: List<Hjemmel> = emptyList(),

    override val order: Order = Order.ASC,
    override val offset: Int,
    override val limit: Int,
    override val sortField: SortField = SortField.FRIST,

    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : BasicSearchCriteria, PageableSearchCriteria, SortableSearchCriteria, SecuritySearchCriteria