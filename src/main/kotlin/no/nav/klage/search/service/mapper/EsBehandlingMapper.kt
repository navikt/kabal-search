package no.nav.klage.search.service.mapper


import no.nav.klage.search.clients.klageendret.BehandlingSkjemaV2
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.elasticsearch.EsSaksdokument
import no.nav.klage.search.domain.elasticsearch.EsStatus
import no.nav.klage.search.service.saksbehandler.SaksbehandlerService
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service

@Service
class EsBehandlingMapper(
    private val saksbehandlerService: SaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun mapBehandlingToEsBehandling(behandling: BehandlingSkjemaV2): EsBehandling {
        return EsBehandling(
            behandlingId = behandling.id,
            sakenGjelderFnr = behandling.sakenGjelder.person!!.fnr,
            ytelseId = behandling.ytelse.id,
            typeId = behandling.type.id,
            fagsystemId = behandling.sakFagsystem.id,
            saksnummer = behandling.sakFagsakId,
            innsendt = behandling.innsendtDato,
            sakMottattKaDato = behandling.sakMottattKaDato,
            sendtTilTrygderetten = behandling.sendtTilTrygderetten,
            ageStartDate = behandling.ageStartDate,
            avsluttetAvSaksbehandler = behandling.avsluttetAvSaksbehandlerTidspunkt,
            returnertFraROL = behandling.returnertFraROLTidspunkt,
            frist = behandling.fristDato,
            varsletFrist = behandling.varsletFristDato,
            tildeltSaksbehandlerident = behandling.gjeldendeTildeling?.saksbehandler?.ident,
            medunderskriverident = behandling.medunderskriver?.saksbehandler?.ident,
            medunderskriverFlowStateId = behandling.medunderskriverFlowStateId,
            medunderskriverEnhet = behandling.medunderskriver?.saksbehandler?.enhet?.nr,
            tildeltEnhet = behandling.gjeldendeTildeling?.enhet?.nr,
            hjemmelIdList = behandling.hjemler.map { it.id },

            saksdokumenter = behandling.saksdokumenter.map { EsSaksdokument(it.journalpostId, it.dokumentInfoId) },
            egenAnsatt = behandling.erEgenAnsatt,
            fortrolig = behandling.erFortrolig,
            strengtFortrolig = behandling.erStrengtFortrolig,
            sattPaaVent = behandling.sattPaaVent,
            sattPaaVentExpires = behandling.sattPaaVentExpires,
            sattPaaVentReasonId = behandling.sattPaaVentReasonId,
            status = EsStatus.valueOf(behandling.status.name),
            feilregistrert = behandling.feilregistrert,
            rolIdent = behandling.rolIdent,
            rolFlowStateId = behandling.rolFlowStateId,
        )
    }
}

