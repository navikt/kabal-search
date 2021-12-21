package no.nav.klage.search.domain.elasticsearch

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class EsKlagebehandling(
    val id: String,

    val klagerFnr: String? = null,
    val klagerNavn: String? = null,
    val klagerFornavn: String? = null,
    val klagerMellomnavn: String? = null,
    val klagerEtternavn: String? = null,
    val klagerOrgnr: String? = null,
    val klagerOrgnavn: String? = null,
    val sakenGjelderFnr: String? = null,
    val sakenGjelderNavn: String? = null,
    val sakenGjelderFornavn: String? = null,
    val sakenGjelderMellomnavn: String? = null,
    val sakenGjelderEtternavn: String? = null,
    val sakenGjelderOrgnr: String? = null,
    val sakenGjelderOrgnavn: String? = null,

    val tema: String,
    val ytelseId: String?,
    val type: String,

    val kildeReferanse: String? = null,
    val sakFagsystem: String? = null,
    val sakFagsystemNavn: String? = null,
    val sakFagsakId: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    val innsendt: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    val mottattFoersteinstans: LocalDate? = null,

    val avsenderSaksbehandleridentFoersteinstans: String? = null,
    val avsenderEnhetFoersteinstans: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val mottattKlageinstans: LocalDateTime,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val tildelt: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val sendtMedunderskriver: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val avsluttet: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val avsluttetAvSaksbehandler: LocalDateTime? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    val frist: LocalDate?,

    val tildeltSaksbehandlerident: String? = null,

    val tildeltSaksbehandlernavn: String? = null,

    val medunderskriverident: String? = null,

    val medunderskriverFlyt: String,

    val tildeltEnhet: String?,

    val hjemler: List<String> = emptyList(),

    val hjemlerNavn: List<String> = emptyList(),

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val created: LocalDateTime,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    var modified: LocalDateTime,

    val kilde: String,

    val saksdokumenter: List<EsSaksdokument> = emptyList(),

    val saksdokumenterJournalpostId: List<String> = emptyList(),

    val saksdokumenterJournalpostIdOgDokumentInfoId: List<String> = emptyList(),

    val egenAnsatt: Boolean = false,

    val fortrolig: Boolean = false,

    val strengtFortrolig: Boolean = false,

/* Enn så lenge har vi bare ett vedtak, og da er det enklere å søke på det når det er flatt her nede enn når det er nested i List<Vedtak>.. */
    val vedtakUtfall: String? = null,

    val vedtakUtfallNavn: String? = null,

    val vedtakHjemler: List<String> = emptyList(),

    val vedtakHjemlerNavn: List<String> = emptyList(),

    val status: Status
) {
    enum class Status {
        IKKE_TILDELT, TILDELT, MEDUNDERSKRIVER_VALGT, SENDT_TIL_MEDUNDERSKRIVER, RETURNERT_TIL_SAKSBEHANDLER, AVSLUTTET_AV_SAKSBEHANDLER, FULLFOERT, UKJENT
    }
}

