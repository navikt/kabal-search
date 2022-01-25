package no.nav.klage.search.api.mapper

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.api.view.AccessView
import no.nav.klage.search.domain.elasticsearch.EsAnonymBehandling
import no.nav.klage.search.service.saksbehandler.InnloggetSaksbehandlerService
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import org.springframework.stereotype.Component

@Component
class AccessMapper(
    private val oAuthTokenService: OAuthTokenService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService
) {

    fun mapAccess(esKlagebehandling: EsAnonymBehandling): AccessView {
        return when {
            harSkriveTilgang(esKlagebehandling) -> AccessView.WRITE
            kanTildelesOppgaven(esKlagebehandling) -> AccessView.ASSIGN
            harLeseTilgang(esKlagebehandling) -> AccessView.READ
            else -> AccessView.NONE
        }
    }

    fun kanTildelesOppgaven(esKlagebehandling: EsAnonymBehandling): Boolean =
        harLeseTilgang(esKlagebehandling) && innloggetSaksbehandlerService.getEnhetMedYtelserForSaksbehandler().ytelser.contains(
            Ytelse.of(esKlagebehandling.ytelseId!!)
        )

    private fun harSkriveTilgang(esKlagebehandling: EsAnonymBehandling): Boolean =
        esKlagebehandling.tildeltSaksbehandlerident != null && esKlagebehandling.tildeltSaksbehandlerident == oAuthTokenService.getInnloggetIdent()

    private fun harLeseTilgang(esKlagebehandling: EsAnonymBehandling): Boolean {

        val kanBehandleStrengtFortrolig = oAuthTokenService.kanBehandleStrengtFortrolig()
        val kanBehandleFortrolig = oAuthTokenService.kanBehandleFortrolig()
        val kanBehandleEgenAnsatt = oAuthTokenService.kanBehandleEgenAnsatt()

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