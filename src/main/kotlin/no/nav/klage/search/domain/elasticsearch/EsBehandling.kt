package no.nav.klage.search.domain.elasticsearch

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime

enum class EsStatus {
    IKKE_TILDELT,
    TILDELT,
    MEDUNDERSKRIVER_VALGT,
    SENDT_TIL_MEDUNDERSKRIVER,
    RETURNERT_TIL_SAKSBEHANDLER,
    AVSLUTTET_AV_SAKSBEHANDLER,
    FULLFOERT,
    UKJENT,
    SATT_PAA_VENT,
    FEILREGISTRERT,
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EsBehandling(
    override val behandlingId: String,
    val sakenGjelderFnr: String,
    override val ytelseId: String,
    override val typeId: String,

    val fagsystemId: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    val innsendt: LocalDate? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val sakMottattKaDato: LocalDateTime,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val avsluttetAvSaksbehandler: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    override val frist: LocalDate?,

    override val tildeltSaksbehandlerident: String? = null,

    override val tildeltSaksbehandlernavn: String? = null,

    override val medunderskriverident: String? = null,

    override val medunderskriverFlytId: String,

    override val medunderskriverEnhet: String? = null,

    val tildeltEnhet: String?,

    override val hjemmelIdList: List<String> = emptyList(),

    val saksdokumenter: List<EsSaksdokument> = emptyList(),

    override val egenAnsatt: Boolean = false,

    override val fortrolig: Boolean = false,

    override val strengtFortrolig: Boolean = false,

    override val utfallId: String? = null,

    override val sattPaaVent: LocalDate? = null,

    override val sattPaaVentExpires: LocalDate? = null,

    override val sattPaaVentReason: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val feilregistrert: LocalDateTime? = null,

    override val rolIdent: String?,

    override val rolStateId: String?,

    val status: EsStatus,
) : EsAnonymBehandling

interface EsAnonymBehandling {

    val strengtFortrolig: Boolean
    val fortrolig: Boolean
    val egenAnsatt: Boolean
    val avsluttetAvSaksbehandler: LocalDateTime?
    val utfallId: String?
    val tildeltSaksbehandlernavn: String?
    val tildeltSaksbehandlerident: String?
    val medunderskriverFlytId: String
    val medunderskriverident: String?
    val medunderskriverEnhet: String?
    val sakMottattKaDato: LocalDateTime
    val frist: LocalDate?
    val hjemmelIdList: List<String>
    val ytelseId: String
    val typeId: String
    val behandlingId: String
    val sattPaaVent: LocalDate?
    val sattPaaVentExpires: LocalDate?
    val sattPaaVentReason: String?
    val feilregistrert: LocalDateTime?
    val rolIdent: String?
    val rolStateId: String?
}
