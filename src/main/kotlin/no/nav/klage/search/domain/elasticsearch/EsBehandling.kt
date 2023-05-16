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
    override val id: String,

    val klagerFnr: String? = null,
    val klagerNavn: String? = null,
    val klagerFornavn: String? = null,
    val klagerMellomnavn: String? = null,
    val klagerEtternavn: String? = null,
    val klagerOrgnr: String? = null,
    val klagerOrgnavn: String? = null,
    val sakenGjelderFnr: String,
    val sakenGjelderNavn: String,
    val sakenGjelderFornavn: String,
    val sakenGjelderMellomnavn: String? = null,
    val sakenGjelderEtternavn: String,
    val sakenGjelderOrgnr: String? = null,
    val sakenGjelderOrgnavn: String? = null,

    override val tema: String,
    override val ytelseId: String,
    override val type: String,

    val kildeReferanse: String? = null,
    val sakFagsystem: String? = null,
    val sakFagsystemNavn: String? = null,
    val sakFagsakId: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    val innsendt: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    val mottattFoersteinstans: LocalDate? = null,

    //Tilsvarer de to under, beholder begge for redundans
    val forrigeSaksbehandlerident: String? = null,
    val forrigeBehandlendeEnhet: String? = null,

    val avsenderSaksbehandleridentFoersteinstans: String? = null,
    val avsenderEnhetFoersteinstans: String? = null,

    //Nytt navn på mottattKlageinstans, beholder begge for redundans
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val sakMottattKaDato: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val mottattKlageinstans: LocalDateTime,

    //Nytt felt, brukes kun til ankebehandling.
    val forrigeVedtaksDato: LocalDate? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val tildelt: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val sendtMedunderskriver: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val avsluttet: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val avsluttetAvSaksbehandler: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    override val frist: LocalDate?,

    override val tildeltSaksbehandlerident: String? = null,

    override val tildeltSaksbehandlernavn: String? = null,

    override val medunderskriverident: String? = null,

    override val medunderskriverNavn: String? = null,

    override val medunderskriverFlyt: String,

    val tildeltEnhet: String?,

    override val hjemler: List<String> = emptyList(),

    val hjemlerNavn: List<String> = emptyList(),

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val created: LocalDateTime,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    var modified: LocalDateTime,

    val kilde: String,

    val saksdokumenter: List<EsSaksdokument> = emptyList(),

    val saksdokumenterJournalpostId: List<String> = emptyList(),

    val saksdokumenterJournalpostIdOgDokumentInfoId: List<String> = emptyList(),

    override val egenAnsatt: Boolean = false,

    override val fortrolig: Boolean = false,

    override val strengtFortrolig: Boolean = false,

    /* Enn så lenge har vi bare ett vedtak, og da er det enklere å søke på det når det er flatt her nede enn når det er nested i List<Vedtak>.. */
    override val vedtakUtfall: String? = null,

    val vedtakUtfallNavn: String? = null,

    val vedtakHjemler: List<String> = emptyList(),

    val vedtakHjemlerNavn: List<String> = emptyList(),

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val sattPaaVent: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val sattPaaVentExpires: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    override val feilregistrert: LocalDateTime? = null,

    val status: EsStatus
) : EsAnonymBehandling

interface EsAnonymBehandling {

    val strengtFortrolig: Boolean
    val fortrolig: Boolean
    val egenAnsatt: Boolean
    val avsluttetAvSaksbehandler: LocalDateTime?
    val vedtakUtfall: String?
    val tildeltSaksbehandlernavn: String?
    val tildeltSaksbehandlerident: String?
    val medunderskriverFlyt: String
    val medunderskriverident: String?
    val medunderskriverNavn: String?
    val mottattKlageinstans: LocalDateTime
    val frist: LocalDate?
    val hjemler: List<String>
    val ytelseId: String
    val tema: String
    val type: String
    val id: String
    val sattPaaVent: LocalDateTime?
    val sattPaaVentExpires: LocalDateTime?
    val feilregistrert: LocalDateTime?
}
