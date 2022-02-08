package no.nav.klage.search.service.mapper


import no.nav.klage.search.clients.egenansatt.EgenAnsattService
import no.nav.klage.search.clients.ereg.EregClient
import no.nav.klage.search.clients.klageendret.BehandlingSkjemaV2
import no.nav.klage.search.clients.klageendret.KlagebehandlingSkjemaV1
import no.nav.klage.search.clients.pdl.PdlFacade
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.elasticsearch.EsSaksdokument
import no.nav.klage.search.domain.elasticsearch.EsStatus
import no.nav.klage.search.service.saksbehandler.SaksbehandlerService
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class EsBehandlingMapper(
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService,
    private val saksbehandlerService: SaksbehandlerService,
    private val eregClient: EregClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapKlagebehandlingToEsKlagebehandling(klagebehandling: KlagebehandlingSkjemaV1): EsBehandling {
        val klagerFnr = klagebehandling.klager.person?.fnr
        val klagerPersonInfo = klagerFnr?.let { pdlFacade.getPersonInfo(it) }

        val klagerOrgnr = klagebehandling.klager.organisasjon?.orgnr
        val klagerOrgnavn = klagerOrgnr?.let { eregClient.hentOrganisasjon(it)?.navn?.sammensattNavn() }

        val sakenGjelderFnr = klagebehandling.sakenGjelder.person?.fnr
        val sakenGjelderPersonInfo = sakenGjelderFnr?.let { pdlFacade.getPersonInfo(it) }

        val sakenGjelderOrgnr = klagebehandling.sakenGjelder.organisasjon?.orgnr
        val sakenGjelderOrgnavn = sakenGjelderOrgnr?.let { eregClient.hentOrganisasjon(it)?.navn?.sammensattNavn() }


        val erFortrolig = sakenGjelderPersonInfo?.harBeskyttelsesbehovFortrolig() ?: false
        val erStrengtFortrolig = sakenGjelderPersonInfo?.harBeskyttelsesbehovStrengtFortrolig() ?: false
        val erEgenAnsatt = sakenGjelderFnr?.let { egenAnsattService.erEgenAnsatt(it) } ?: false

        return EsBehandling(
            id = klagebehandling.id,
            klagerFnr = klagerFnr,
            klagerNavn = klagerPersonInfo?.sammensattNavn,
            klagerFornavn = klagerPersonInfo?.fornavn,
            klagerMellomnavn = klagerPersonInfo?.mellomnavn,
            klagerEtternavn = klagerPersonInfo?.etternavn,
            klagerOrgnr = klagerOrgnr,
            klagerOrgnavn = klagerOrgnavn,
            sakenGjelderFnr = sakenGjelderFnr,
            sakenGjelderNavn = sakenGjelderPersonInfo?.sammensattNavn,
            sakenGjelderFornavn = sakenGjelderPersonInfo?.fornavn,
            sakenGjelderMellomnavn = sakenGjelderPersonInfo?.mellomnavn,
            sakenGjelderEtternavn = sakenGjelderPersonInfo?.etternavn,
            sakenGjelderOrgnr = sakenGjelderOrgnr,
            sakenGjelderOrgnavn = sakenGjelderOrgnavn,
            tema = klagebehandling.tema.id,
            ytelseId = klagebehandling.ytelse?.id,
            type = klagebehandling.type.id,
            kildeReferanse = klagebehandling.kildeReferanse,
            sakFagsystem = klagebehandling.sakFagsystem?.id,
            sakFagsakId = klagebehandling.sakFagsakId,
            innsendt = klagebehandling.innsendtDato,
            mottattFoersteinstans = klagebehandling.mottattFoersteinstansDato,
            avsenderSaksbehandleridentFoersteinstans = klagebehandling.avsenderSaksbehandlerFoersteinstans?.ident,
            avsenderEnhetFoersteinstans = klagebehandling.avsenderEnhetFoersteinstans?.nr,
            forrigeSaksbehandlerident = klagebehandling.avsenderSaksbehandlerFoersteinstans?.ident,
            forrigeBehandlendeEnhet = klagebehandling.avsenderEnhetFoersteinstans?.nr,
            sakMottattKaDato = klagebehandling.mottattKlageinstansTidspunkt,
            forrigeVedtaksDato = null,
            mottattKlageinstans = klagebehandling.mottattKlageinstansTidspunkt,
            tildelt = klagebehandling.gjeldendeTildeling?.tidspunkt,
            avsluttet = klagebehandling.avsluttetTidspunkt,
            avsluttetAvSaksbehandler = klagebehandling.avsluttetAvSaksbehandlerTidspunkt,
            frist = klagebehandling.fristDato,
            tildeltSaksbehandlerident = klagebehandling.gjeldendeTildeling?.saksbehandler?.ident,
            tildeltSaksbehandlernavn = getTildeltSaksbehandlernavn(klagebehandling),
            medunderskriverident = klagebehandling.medunderskriver?.saksbehandler?.ident,
            medunderskriverFlyt = klagebehandling.medunderskriverFlytStatus.navn,
            sendtMedunderskriver = klagebehandling.medunderskriver?.tidspunkt,
            tildeltEnhet = klagebehandling.gjeldendeTildeling?.enhet?.nr,
            hjemler = klagebehandling.hjemler.map { it.id },
            created = klagebehandling.opprettetTidspunkt,
            modified = klagebehandling.sistEndretTidspunkt,
            kilde = klagebehandling.kildesystem.id,
            saksdokumenter = klagebehandling.saksdokumenter.map { EsSaksdokument(it.journalpostId, it.dokumentInfoId) },
            saksdokumenterJournalpostId = klagebehandling.saksdokumenter.map { it.journalpostId },
            saksdokumenterJournalpostIdOgDokumentInfoId = klagebehandling.saksdokumenter.map {
                it.journalpostId + it.dokumentInfoId
            },
            egenAnsatt = erEgenAnsatt,
            fortrolig = erFortrolig,
            strengtFortrolig = erStrengtFortrolig,
            vedtakUtfall = klagebehandling.vedtak?.utfall?.id,
            vedtakHjemler = klagebehandling.vedtak?.hjemler?.map { hjemmel -> hjemmel.id } ?: emptyList(),
            hjemlerNavn = klagebehandling.hjemler.map { it.navn },
            vedtakUtfallNavn = klagebehandling.vedtak?.utfall?.navn,
            sakFagsystemNavn = klagebehandling.sakFagsystem?.navn,
            status = EsStatus.valueOf(klagebehandling.status.name),
        )
    }

    fun mapBehandlingToEsBehandling(behandling: BehandlingSkjemaV2): EsBehandling {
        val klagerFnr = behandling.klager.person?.fnr
        val klagerPersonInfo = klagerFnr?.let { pdlFacade.getPersonInfo(it) }

        val klagerOrgnr = behandling.klager.organisasjon?.orgnr
        val klagerOrgnavn = klagerOrgnr?.let { eregClient.hentOrganisasjon(it)?.navn?.sammensattNavn() }

        val sakenGjelderFnr = behandling.sakenGjelder.person?.fnr
        val sakenGjelderPersonInfo = sakenGjelderFnr?.let { pdlFacade.getPersonInfo(it) }

        val sakenGjelderOrgnr = behandling.sakenGjelder.organisasjon?.orgnr
        val sakenGjelderOrgnavn = sakenGjelderOrgnr?.let { eregClient.hentOrganisasjon(it)?.navn?.sammensattNavn() }


        val erFortrolig = sakenGjelderPersonInfo?.harBeskyttelsesbehovFortrolig() ?: false
        val erStrengtFortrolig = sakenGjelderPersonInfo?.harBeskyttelsesbehovStrengtFortrolig() ?: false
        val erEgenAnsatt = sakenGjelderFnr?.let { egenAnsattService.erEgenAnsatt(it) } ?: false

        return EsBehandling(
            id = behandling.id,
            klagerFnr = klagerFnr,
            klagerNavn = klagerPersonInfo?.sammensattNavn,
            klagerFornavn = klagerPersonInfo?.fornavn,
            klagerMellomnavn = klagerPersonInfo?.mellomnavn,
            klagerEtternavn = klagerPersonInfo?.etternavn,
            klagerOrgnr = klagerOrgnr,
            klagerOrgnavn = klagerOrgnavn,
            sakenGjelderFnr = sakenGjelderFnr,
            sakenGjelderNavn = sakenGjelderPersonInfo?.sammensattNavn,
            sakenGjelderFornavn = sakenGjelderPersonInfo?.fornavn,
            sakenGjelderMellomnavn = sakenGjelderPersonInfo?.mellomnavn,
            sakenGjelderEtternavn = sakenGjelderPersonInfo?.etternavn,
            sakenGjelderOrgnr = sakenGjelderOrgnr,
            sakenGjelderOrgnavn = sakenGjelderOrgnavn,
            tema = behandling.tema.id,
            ytelseId = behandling.ytelse.id,
            type = behandling.type.id,
            kildeReferanse = behandling.kildeReferanse,
            sakFagsystem = behandling.sakFagsystem?.id,
            sakFagsakId = behandling.sakFagsakId,
            innsendt = behandling.innsendtDato,
            mottattFoersteinstans = behandling.mottattFoersteinstansDato,
            avsenderSaksbehandleridentFoersteinstans = behandling.forrigeSaksbehandler?.ident,
            avsenderEnhetFoersteinstans = behandling.forrigeBehandlendeEnhet?.nr,
            forrigeSaksbehandlerident = behandling.forrigeSaksbehandler?.ident,
            forrigeBehandlendeEnhet = behandling.forrigeBehandlendeEnhet?.nr,
            sakMottattKaDato = behandling.sakMottattKaDato,
            forrigeVedtaksDato = behandling.forrigeVedtaksDato,
            mottattKlageinstans = behandling.sakMottattKaDato,
            tildelt = behandling.gjeldendeTildeling?.tidspunkt,
            avsluttet = behandling.avsluttetTidspunkt,
            avsluttetAvSaksbehandler = behandling.avsluttetAvSaksbehandlerTidspunkt,
            frist = behandling.fristDato,
            tildeltSaksbehandlerident = behandling.gjeldendeTildeling?.saksbehandler?.ident,
            tildeltSaksbehandlernavn = getTildeltSaksbehandlernavn(behandling),
            medunderskriverident = behandling.medunderskriver?.saksbehandler?.ident,
            medunderskriverFlyt = behandling.medunderskriverFlytStatus.navn,
            sendtMedunderskriver = behandling.medunderskriver?.tidspunkt,
            tildeltEnhet = behandling.gjeldendeTildeling?.enhet?.nr,
            hjemler = behandling.hjemler.map { it.id },
            created = behandling.opprettetTidspunkt,
            modified = behandling.sistEndretTidspunkt,
            kilde = behandling.kildesystem.id,
            saksdokumenter = behandling.saksdokumenter.map { EsSaksdokument(it.journalpostId, it.dokumentInfoId) },
            saksdokumenterJournalpostId = behandling.saksdokumenter.map { it.journalpostId },
            saksdokumenterJournalpostIdOgDokumentInfoId = behandling.saksdokumenter.map {
                it.journalpostId + it.dokumentInfoId
            },
            egenAnsatt = erEgenAnsatt,
            fortrolig = erFortrolig,
            strengtFortrolig = erStrengtFortrolig,
            vedtakUtfall = behandling.vedtak?.utfall?.id,
            vedtakHjemler = behandling.vedtak?.hjemler?.map { hjemmel -> hjemmel.id } ?: emptyList(),
            hjemlerNavn = behandling.hjemler.map { it.navn },
            vedtakUtfallNavn = behandling.vedtak?.utfall?.navn,
            sakFagsystemNavn = behandling.sakFagsystem?.navn,
            sattPaaVent = behandling.sattPaaVent,
            sattPaaVentExpires = behandling.sattPaaVentExpires,
            status = EsStatus.valueOf(behandling.status.name),
        )
    }

    private fun getTildeltSaksbehandlernavn(klagebehandling: KlagebehandlingSkjemaV1): String? {
        return klagebehandling.gjeldendeTildeling?.saksbehandler?.ident?.let {
            val names = saksbehandlerService.getNamesForSaksbehandlere(setOf(it))
            names[it]
        }
    }

    private fun getTildeltSaksbehandlernavn(behandling: BehandlingSkjemaV2): String? {
        return behandling.gjeldendeTildeling?.saksbehandler?.ident?.let {
            val names = saksbehandlerService.getNamesForSaksbehandlere(setOf(it))
            names[it]
        }
    }
}

