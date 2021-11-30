package no.nav.klage.search.domain

import no.nav.klage.kodeverk.Hjemmel
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import java.time.LocalDate
import java.time.LocalDateTime

data class KlagebehandlingerSearchCriteria(
    val typer: List<Type> = emptyList(),
    val ytelser: List<Ytelse> = emptyList(),
    val hjemler: List<Hjemmel> = emptyList(),
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

    val order: Order? = null,
    val offset: Int,
    val limit: Int,
    val erTildeltSaksbehandler: Boolean? = null,
    val saksbehandlere: List<String> = emptyList(),
    val enhetId: String? = null,
    val projection: Projection? = null,
    val sortField: SortField? = null
) {

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


    enum class Projection {
        UTVIDET
    }

    fun isProjectionUtvidet(): Boolean = Projection.UTVIDET == projection


    fun isFnrSoek() = raw.isNumeric()

    private fun String.isNumeric() = toLongOrNull() != null
}
