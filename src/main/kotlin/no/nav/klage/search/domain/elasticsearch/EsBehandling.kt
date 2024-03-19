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
    val saksnummer: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    val innsendt: LocalDate? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val sakMottattKaDato: LocalDateTime,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val avsluttetAvSaksbehandler: LocalDateTime?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val returnertFraROL: LocalDateTime?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    override val frist: LocalDate?,

    override val tildeltSaksbehandlerident: String?,

    override val tildeltSaksbehandlernavn: String?,

    override val medunderskriverident: String?,

    override val medunderskriverNavn: String?,

    override val medunderskriverFlowStateId: String,

    override val medunderskriverEnhet: String?,

    val tildeltEnhet: String?,

    override val hjemmelIdList: List<String> = emptyList(),

    val saksdokumenter: List<EsSaksdokument> = emptyList(),

    override val egenAnsatt: Boolean = false,

    override val fortrolig: Boolean = false,

    override val strengtFortrolig: Boolean = false,

    override val utfallId: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    override val sattPaaVent: LocalDate? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    override val sattPaaVentExpires: LocalDate? = null,

    override val sattPaaVentReason: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val feilregistrert: LocalDateTime? = null,

    override val rolIdent: String?,

    override val rolNavn: String?,

    override val rolFlowStateId: String,

    val status: EsStatus,
) : EsAnonymBehandling

interface EsAnonymBehandling {

    val strengtFortrolig: Boolean
    val fortrolig: Boolean
    val egenAnsatt: Boolean
    val avsluttetAvSaksbehandler: LocalDateTime?
    val returnertFraROL: LocalDateTime?
    val utfallId: String?
    val tildeltSaksbehandlernavn: String?
    val tildeltSaksbehandlerident: String?
    val medunderskriverFlowStateId: String
    val medunderskriverident: String?
    val medunderskriverNavn: String?
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
    val rolNavn: String?
    val rolFlowStateId: String?
}
