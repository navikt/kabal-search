package no.nav.klage.search.service

import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.search.clients.kabalinnstillinger.InnstillingerView
import no.nav.klage.search.clients.kabalinnstillinger.KabalInnstillingerClient
import no.nav.klage.search.domain.saksbehandler.Innstillinger
import org.springframework.stereotype.Service

@Service
class KabalInnstillingerService(
    private val kabalInnstillingerClient: KabalInnstillingerClient,
) {
    fun getInnstillingerForCurrentSaksbehandler(): Innstillinger {
        return mapToInnstillinger(kabalInnstillingerClient.getInnloggetSaksbehandlersInnstillinger())
    }

    private fun mapToInnstillinger(innstillingerView: InnstillingerView): Innstillinger {
        return Innstillinger(
            hjemler = innstillingerView.hjemler.map { Hjemmel.of(it) },
            ytelser = innstillingerView.ytelser.map { Ytelse.of(it) },
        )
    }


}