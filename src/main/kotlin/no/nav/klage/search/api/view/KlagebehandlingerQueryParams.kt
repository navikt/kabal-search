package no.nav.klage.search.api.view

import java.time.LocalDate

interface CommonOppgaverQueryParams {
    var typer: List<String>
    var ytelser: List<String>
    var hjemler: List<String>
    val rekkefoelge: Rekkefoelge?
    val sortering: Sortering?
    val fristFrom: LocalDate?
    val fristTo: LocalDate?
    val varsletFristFrom: LocalDate?
    val varsletFristTo: LocalDate?
}

interface FerdigstilteOppgaverQueryParams {
    val ferdigstiltFrom: LocalDate?
    val ferdigstiltTo: LocalDate?
}

interface ReturnerteROLOppgaverQueryParams {
    val returnertFrom: LocalDate?
    val returnertTo: LocalDate?
}

data class MineFerdigstilteOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.AVSLUTTET_AV_SAKSBEHANDLER,
    override val ferdigstiltFrom: LocalDate?,
    override val ferdigstiltTo: LocalDate?,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
) : CommonOppgaverQueryParams, FerdigstilteOppgaverQueryParams

data class MineReturnerteROLOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.RETURNERT_FRA_ROL,
    override val returnertFrom: LocalDate?,
    override val returnertTo: LocalDate?,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
) : CommonOppgaverQueryParams, ReturnerteROLOppgaverQueryParams

data class MineUferdigeOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
) : CommonOppgaverQueryParams

data class MineLedigeOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
) : CommonOppgaverQueryParams

data class MineLedigeOppgaverCountQueryParams(
    var typer: List<String> = emptyList(),
    var ytelser: List<String> = emptyList(),
    var hjemler: List<String> = emptyList(),
)

data class MineOppgaverPaaVentQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
) : CommonOppgaverQueryParams

data class EnhetensFerdigstilteOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.AVSLUTTET_AV_SAKSBEHANDLER,
    override val ferdigstiltFrom: LocalDate?,
    override val ferdigstiltTo: LocalDate?,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
    var tildelteSaksbehandlere: List<String> = emptyList(),
) : CommonOppgaverQueryParams, FerdigstilteOppgaverQueryParams

data class EnhetensOppgaverPaaVentQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
    var tildelteSaksbehandlere: List<String> = emptyList(),
    var medunderskrivere: List<String> = emptyList(),
) : CommonOppgaverQueryParams

data class EnhetensUferdigeOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
    var tildelteSaksbehandlere: List<String> = emptyList(),
    var medunderskrivere: List<String> = emptyList(),
) : CommonOppgaverQueryParams

data class KrolsUferdigeOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.FRIST,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
    var tildelteRol: List<String> = emptyList(),
) : CommonOppgaverQueryParams

data class KrolsReturnerteOppgaverQueryParams(
    override var typer: List<String> = emptyList(),
    override var ytelser: List<String> = emptyList(),
    override var hjemler: List<String> = emptyList(),
    override val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    override val sortering: Sortering? = Sortering.RETURNERT_FRA_ROL,
    override val returnertFrom: LocalDate?,
    override val returnertTo: LocalDate?,
    override val fristFrom: LocalDate?,
    override val fristTo: LocalDate?,
    override val varsletFristFrom: LocalDate?,
    override val varsletFristTo: LocalDate?,
) : CommonOppgaverQueryParams, ReturnerteROLOppgaverQueryParams

enum class Rekkefoelge {
    STIGENDE, SYNKENDE
}

enum class Sortering {
    FRIST, MOTTATT, ALDER, PAA_VENT_FROM, PAA_VENT_TO, AVSLUTTET_AV_SAKSBEHANDLER, RETURNERT_FRA_ROL, VARSLET_FRIST
}