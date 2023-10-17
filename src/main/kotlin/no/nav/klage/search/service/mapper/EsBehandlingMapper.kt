package no.nav.klage.search.service.mapper


import no.nav.klage.search.clients.egenansatt.EgenAnsattService
import no.nav.klage.search.clients.klageendret.BehandlingSkjemaV2
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
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapBehandlingToEsBehandling(behandling: BehandlingSkjemaV2): EsBehandling {
        val sakenGjelderFnr = behandling.sakenGjelder.person!!.fnr
        val sakenGjelderPersonInfo = pdlFacade.getPersonInfo(sakenGjelderFnr)
        val erFortrolig = sakenGjelderPersonInfo.harBeskyttelsesbehovFortrolig()
        val erStrengtFortrolig = sakenGjelderPersonInfo.harBeskyttelsesbehovStrengtFortrolig()
        val erEgenAnsatt = sakenGjelderFnr.let { egenAnsattService.erEgenAnsatt(it) }

        return EsBehandling(
            behandlingId = behandling.id,
            sakenGjelderFnr = sakenGjelderFnr,
            ytelseId = behandling.ytelse.id,
            typeId = behandling.type.id,
            fagsystemId = behandling.sakFagsystem.id,
            saksnummer = behandling.sakFagsakId,
            innsendt = behandling.innsendtDato,
            sakMottattKaDato = behandling.sakMottattKaDato,
            avsluttetAvSaksbehandler = behandling.avsluttetAvSaksbehandlerTidspunkt,
            returnertFraROL = behandling.returnertFraROLTidspunkt,
            frist = behandling.fristDato,
            tildeltSaksbehandlerident = behandling.gjeldendeTildeling?.saksbehandler?.ident,
            tildeltSaksbehandlernavn = getSaksbehandlernavn(behandling.gjeldendeTildeling?.saksbehandler?.ident),
            medunderskriverident = behandling.medunderskriver?.saksbehandler?.ident,
            medunderskriverNavn = getSaksbehandlernavn(behandling.medunderskriver?.saksbehandler?.ident),
            medunderskriverFlowStateId = behandling.medunderskriverFlowStateId,
            medunderskriverEnhet = getEnhetsnummerForNavIdent(behandling.medunderskriver?.saksbehandler?.ident),
            tildeltEnhet = behandling.gjeldendeTildeling?.enhet?.nr,
            hjemmelIdList = behandling.hjemler.map { it.id },

            saksdokumenter = behandling.saksdokumenter.map { EsSaksdokument(it.journalpostId, it.dokumentInfoId) },
            egenAnsatt = erEgenAnsatt,
            fortrolig = erFortrolig,
            strengtFortrolig = erStrengtFortrolig,
            utfallId = behandling.vedtak?.utfall?.id,
            sattPaaVent = behandling.sattPaaVent,
            sattPaaVentExpires = behandling.sattPaaVentExpires,
            sattPaaVentReason = behandling.sattPaaVentReason,
            status = EsStatus.valueOf(behandling.status.name),
            feilregistrert = behandling.feilregistrert,
            rolIdent = behandling.rolIdent,
            rolNavn = getSaksbehandlernavn(behandling.rolIdent),
            rolFlowStateId = behandling.rolFlowStateId,
        )
    }

    private fun getSaksbehandlernavn(navIdent: String?): String? {
        return navIdent?.let {
            saksbehandlerService.getNameForIdent(it)
        }
    }

    private fun getEnhetsnummerForNavIdent(navIdent: String?): String? {
        return navIdent?.let {
            saksbehandlerService.getEnhetsnummerForNavIdent(it)
        }
    }
}

