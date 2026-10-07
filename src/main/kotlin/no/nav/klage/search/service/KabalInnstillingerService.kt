package no.nav.klage.search.service

import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.search.clients.kabalinnstillinger.InnstillingerView
import no.nav.klage.search.clients.kabalinnstillinger.KabalInnstillingerClient
import no.nav.klage.search.clients.kabalinnstillinger.SaksbehandlerAccessView
import no.nav.klage.search.domain.saksbehandler.Innstillinger
import no.nav.klage.search.domain.saksbehandler.SaksbehandlerAccess
import org.springframework.stereotype.Service

@Service
class KabalInnstillingerService(
    private val kabalInnstillingerClient: KabalInnstillingerClient,
) {
    fun getInnstillingerForCurrentSaksbehandler(): Innstillinger =
        mapToInnstillinger(kabalInnstillingerClient.getInnloggetSaksbehandlersInnstillinger())

    fun getSaksbehandlersAccess(navIdent: String): SaksbehandlerAccess =
        mapToSaksbehandlerAccess(kabalInnstillingerClient.getSaksbehandlersAccess(navIdent))

    private fun mapToSaksbehandlerAccess(saksbehandlerAccessView: SaksbehandlerAccessView): SaksbehandlerAccess =
        SaksbehandlerAccess(
            saksbehandlerIdent = saksbehandlerAccessView.saksbehandlerIdent,
            saksbehandlerName = saksbehandlerAccessView.saksbehandlerName,
            ytelser = saksbehandlerAccessView.ytelseIdList.map { Ytelse.of(it) },
            anketeam = saksbehandlerAccessView.anketeam,
            created = saksbehandlerAccessView.created,
            accessRightsModified = saksbehandlerAccessView.accessRightsModified,
        )

    private fun mapToInnstillinger(innstillingerView: InnstillingerView): Innstillinger =
        Innstillinger(
            hjemler = innstillingerView.hjemler.map { Hjemmel.of(it) },
            ytelser = innstillingerView.ytelser.map { Ytelse.of(it) },
        )
}
