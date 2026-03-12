package no.nav.klage.search.service.saksbehandler

import no.nav.klage.search.clients.klagelookup.KlageLookupClient
import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerService(
    private val klageLookupClient: KlageLookupClient,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getEnhetForSaksbehandler(): Enhet {
        val foundEnhet = klageLookupClient.getUserInfo(navIdent = tokenUtil.getIdent()).enhet
        return Enhet(
            enhetId = foundEnhet.enhetNr,
            navn = foundEnhet.enhetNavn,
        )
    }
}
