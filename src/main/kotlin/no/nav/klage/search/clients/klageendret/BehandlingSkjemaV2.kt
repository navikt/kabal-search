package no.nav.klage.search.clients.klageendret

import java.time.LocalDate
import java.time.LocalDateTime

class BehandlingSkjemaV2(
    val id: String,
    val klager: PersonEllerOrganisasjon,
    val klagersProsessfullmektig: PersonEllerOrganisasjon?,
    val sakenGjelder: PersonEllerOrganisasjon,
    val tema: Kode,
    val ytelse: Kode,
    val type: Kode,
    val kildeReferanse: String,
    val sakFagsystem: Kode?,
    val sakFagsakId: String?,
    val innsendtDato: LocalDate?,
    val mottattFoersteinstansDato: LocalDate?,
    val forrigeSaksbehandler: Saksbehandler?,
    val forrigeBehandlendeEnhet: Enhet?,
    val forrigeVedtaksDato: LocalDate?,
    val sakMottattKaDato: LocalDateTime,
    val avsluttetAvSaksbehandlerTidspunkt: LocalDateTime?,
    val avsluttetTidspunkt: LocalDateTime?,
    val fristDato: LocalDate?,
    val gjeldendeTildeling: TildeltSaksbehandler?,
    val medunderskriver: TildeltMedunderskriver?,
    val medunderskriverFlytStatus: Kode,
    val hjemler: List<Kode>,
    val opprettetTidspunkt: LocalDateTime,
    val sistEndretTidspunkt: LocalDateTime,
    val kildesystem: Kode,

    val saksdokumenter: List<Dokument>,
    val vedtak: Vedtak?,
    val sattPaaVent: LocalDateTime? = null,
    val sattPaaVentExpires: LocalDateTime? = null,

    val status: StatusType,
)
{
    data class Vedtak(
        val utfall: Kode?,
        val hjemler: List<Kode>,
    )

    enum class StatusType {
        IKKE_TILDELT, TILDELT, MEDUNDERSKRIVER_VALGT, SENDT_TIL_MEDUNDERSKRIVER, RETURNERT_TIL_SAKSBEHANDLER, AVSLUTTET_AV_SAKSBEHANDLER, FULLFOERT, UKJENT, SATT_PAA_VENT
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