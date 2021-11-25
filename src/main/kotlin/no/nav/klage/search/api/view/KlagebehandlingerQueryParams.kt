package no.nav.klage.search.api.view

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class KlagebehandlingerQueryParams(
    var typer: List<String> = emptyList(),
    var temaer: List<String> = emptyList(),
    val ytelser: List<String> = emptyList(),
    var hjemler: List<String> = emptyList(),
    val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    val sortering: Sortering? = Sortering.FRIST,
    val start: Int,
    val antall: Int,
    val projeksjon: Projeksjon? = null,
    val erTildeltSaksbehandler: Boolean? = null,
    val tildeltSaksbehandler: List<String> = emptyList(),
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val ferdigstiltFom: LocalDate? = null,
    val ferdigstiltDaysAgo: Long? = null,
    val enhet: String
) {

    enum class Rekkefoelge {
        STIGENDE, SYNKENDE
    }

    enum class Sortering {
        FRIST, MOTTATT
    }

    enum class Projeksjon {
        UTVIDET
    }
}
