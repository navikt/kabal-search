package no.nav.klage.search.api.view

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class KlagebehandlingerQueryParamsNew(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    val projeksjon: Projeksjon? = null,
    val erTildeltSaksbehandler: Boolean? = null,
    var tildeltSaksbehandler: List<String> = emptyList(),
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val ferdigstiltFom: LocalDate? = null,
    val ferdigstiltDaysAgo: Long? = null,
    val enhet: String,
) : CommonOppgaverQueryParams

data class MineFerdigstilteOppgaverQueryParamsNew(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val ferdigstiltDaysAgo: Long,
) : CommonOppgaverQueryParams, FerdigstilteOppgaverQueryParams

data class MineUferdigeOppgaverQueryParamsNew(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
) : CommonOppgaverQueryParams

data class MineLedigeOppgaverQueryParamsNew(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
) : CommonOppgaverQueryParams

data class MineLedigeOppgaverCountQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.MOTTATT,
) : CommonOppgaverQueryParams

data class MineOppgaverPaaVentQueryParamsNew(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
) : CommonOppgaverQueryParams

data class EnhetensFerdigstilteOppgaverQueryParamsNew(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val ferdigstiltDaysAgo: Long,
    var tildelteSaksbehandlere: List<String> = emptyList(),
) : CommonOppgaverQueryParams, FerdigstilteOppgaverQueryParams

data class EnhetensOppgaverPaaVentQueryParamsNew(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    var tildelteSaksbehandlere: List<String> = emptyList(),
) : CommonOppgaverQueryParams

data class EnhetensUferdigeOppgaverQueryParamsNew(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    var tildelteSaksbehandlere: List<String> = emptyList(),
) : CommonOppgaverQueryParams