package no.nav.klage.search.clients.klageendret

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class BehandlingSkjemaV2(
    val id: String,
    val sakenGjelder: PersonEllerOrganisasjon,
    val ytelse: Kode,
    val type: Kode,
    val sakFagsystem: Kode,
    val sakFagsakId: String,
    val innsendtDato: LocalDate?,
    val sakMottattKaDato: LocalDateTime,
    val avsluttetAvSaksbehandlerTidspunkt: LocalDateTime?,
    val returnertFraROLTidspunkt: LocalDateTime?,
    val fristDato: LocalDate?,
    val varsletFristDato: LocalDate?,
    val gjeldendeTildeling: TildeltSaksbehandler?,
    val medunderskriver: TildeltMedunderskriver?,
    val medunderskriverFlowStateId: String,
    val hjemler: List<Kode>,

    val saksdokumenter: List<Dokument>,
    val vedtak: Vedtak?,
    val sattPaaVent: LocalDate?,
    val sattPaaVentExpires: LocalDate?,
    val sattPaaVentReason: String?,
    val sattPaaVentReasonId: String?,

    val status: StatusType,

    val feilregistrert: LocalDateTime?,
    val rolIdent: String?,
    val rolFlowStateId: String,
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Vedtak(
        val utfall: Kode?,
    )

    enum class StatusType {
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

    data class Person(
        val fnr: String,
    )

    data class Organisasjon(
        val orgnr: String,
    )

    data class PersonEllerOrganisasjon(val person: Person?, val organisasjon: Organisasjon?) {
        constructor(person: Person) : this(person, null)
        constructor(organisasjon: Organisasjon) : this(null, organisasjon)
    }

    data class Kode(
        val id: String,
        val navn: String,
        val beskrivelse: String,
    )

    data class Enhet(
        val nr: String,
    )

    data class Saksbehandler(
        val ident: String,
    )

    data class TildeltSaksbehandler(
        val tidspunkt: LocalDateTime,
        val saksbehandler: Saksbehandler?,
        val enhet: Enhet?,
    )

    data class TildeltMedunderskriver(
        val tidspunkt: LocalDateTime,
        val saksbehandler: Saksbehandler?,
    )

    data class Dokument(
        val journalpostId: String,
        val dokumentInfoId: String,
    )

}