package no.nav.klage.search.api.view

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class KlagebehandlingerQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val start: Int,
    override val antall: Int,
    val projeksjon: Projeksjon? = null,
    val erTildeltSaksbehandler: Boolean? = null,
    var tildeltSaksbehandler: List<String> = emptyList(),
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    override val ferdigstiltFom: LocalDate? = null,
    override val ferdigstiltDaysAgo: Long? = null,
    val enhet: String
) : CommonOppgaverQueryParams, FerdigstilteOppgaverQueryParams

interface CommonOppgaverQueryParams {
    var typer: List<String>
    var ytelser: List<String>
    var hjemler: List<String>
    val rekkefoelge: Rekkefoelge?
    val sortering: Sortering?
    val start: Int
    val antall: Int
}

interface FerdigstilteOppgaverQueryParams {
    val ferdigstiltFom: LocalDate?
    val ferdigstiltDaysAgo: Long?
}

data class MineFerdigstilteOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val start: Int,
    override val antall: Int,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    override val ferdigstiltFom: LocalDate? = null,
    override val ferdigstiltDaysAgo: Long? = null,
) : CommonOppgaverQueryParams, FerdigstilteOppgaverQueryParams

data class MineUferdigeOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val start: Int,
    override val antall: Int,
) : CommonOppgaverQueryParams

data class EnhetensLedigeOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val start: Int,
    override val antall: Int,
) : CommonOppgaverQueryParams

data class EnhetensFerdigstilteOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val start: Int,
    override val antall: Int,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    override val ferdigstiltFom: LocalDate? = null,
    override val ferdigstiltDaysAgo: Long? = null,
    var tildelteSaksbehandler: List<String> = emptyList(),
) : CommonOppgaverQueryParams, FerdigstilteOppgaverQueryParams

data class EnhetensUferdigeOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val start: Int,
    override val antall: Int,
    var tildelteSaksbehandler: List<String> = emptyList(),
) : CommonOppgaverQueryParams

enum class Rekkefoelge {
    STIGENDE, SYNKENDE
}

enum class Sortering {
    FRIST, MOTTATT, ALDER
}

enum class Projeksjon {
    UTVIDET
}