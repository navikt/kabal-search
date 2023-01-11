package no.nav.klage.search.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.clients.kabalinnstillinger.KabalInnstillingerClient
import org.springframework.stereotype.Service

@Service
class KabalInnstillingerService(
    private val kabalInnstillingerClient: KabalInnstillingerClient,
) {

    //TODO: Bør vi ha et cache her? Kan være et problem om leder gir nye tilganger, kanskje et kortere cache?
    fun getTildelteYtelserForSaksbehandler(navIdent: String): List<Ytelse> {
        return kabalInnstillingerClient.getSaksbehandlersTildelteYtelser(navIdent).ytelseIdList.map {
            Ytelse.of(it)
        }
    }
}