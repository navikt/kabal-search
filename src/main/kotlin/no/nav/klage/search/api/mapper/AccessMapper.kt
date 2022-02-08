package no.nav.klage.search.api.mapper

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.api.view.AccessView
import no.nav.klage.search.domain.elasticsearch.EsAnonymBehandling
import org.springframework.stereotype.Component

@Component
class AccessMapper {

    fun mapAccess(
        esKlagebehandling: EsAnonymBehandling,
        innloggetIdent: String,
        kanBehandleStrengtFortrolig: Boolean,
        kanBehandleFortrolig: Boolean,
        kanBehandleEgenAnsatt: Boolean,
        lovligeYtelser: List<Ytelse>
    ): AccessView {
        return when {
            harSkriveTilgang(
                esKlagebehandling = esKlagebehandling,
                innloggetIdent = innloggetIdent
            ) -> AccessView.WRITE
            kanTildelesOppgaven(
                esKlagebehandling = esKlagebehandling,
                kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig,
                kanBehandleFortrolig = kanBehandleFortrolig,
                kanBehandleEgenAnsatt = kanBehandleEgenAnsatt,
                lovligeYtelser = lovligeYtelser
            ) -> AccessView.ASSIGN
            harLeseTilgang(
                esKlagebehandling = esKlagebehandling,
                kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig,
                kanBehandleFortrolig = kanBehandleFortrolig,
                kanBehandleEgenAnsatt = kanBehandleEgenAnsatt
            ) -> AccessView.READ
            else -> AccessView.NONE
        }
    }

    fun kanTildelesOppgaven(
        esKlagebehandling: EsAnonymBehandling,
        kanBehandleStrengtFortrolig: Boolean,
        kanBehandleFortrolig: Boolean,
        kanBehandleEgenAnsatt: Boolean,
        lovligeYtelser: List<Ytelse>
    ): Boolean =
        harLeseTilgang(
            esKlagebehandling = esKlagebehandling,
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig,
            kanBehandleFortrolig = kanBehandleFortrolig,
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt
        )
                && lovligeYtelser.contains(Ytelse.of(esKlagebehandling.ytelseId!!))

    private fun harSkriveTilgang(esKlagebehandling: EsAnonymBehandling, innloggetIdent: String): Boolean =
        esKlagebehandling.tildeltSaksbehandlerident != null && esKlagebehandling.tildeltSaksbehandlerident == innloggetIdent

    private fun harLeseTilgang(
        esKlagebehandling: EsAnonymBehandling,
        kanBehandleStrengtFortrolig: Boolean,
        kanBehandleFortrolig: Boolean,
        kanBehandleEgenAnsatt: Boolean,
    ): Boolean {

        val erStrengtFortrolig = esKlagebehandling.strengtFortrolig
        val erFortrolig = esKlagebehandling.fortrolig
        val erEgenAnsatt = esKlagebehandling.egenAnsatt

        //Fortrolig og strengt fortrolig trumfer egen ansatt, så man trenger ikke sjekke egenAnsatt hvis behandlingen er strengt fortrolig eller fortrolig
        //Men en med kun fortrolig rettigheter, ikke egenAnsatt rettigheter, vil ikke kunne se en person som bare er egenAnsatt og ikke fortrolig.
        //Poenget er at den saksbehandleren skal kunne se både de personene som er fortrolig, og de som er komboen fortrolig og egenAnsatt.
        return when {
            erStrengtFortrolig && kanBehandleStrengtFortrolig -> true
            erFortrolig && kanBehandleFortrolig -> true
            erEgenAnsatt && kanBehandleEgenAnsatt -> true
            else -> true
        }
    }
}