package no.nav.klage.search.gateway

import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeTemaer
import no.nav.klage.search.domain.saksbehandler.SaksbehandlerIdent

interface AxsysGateway {

    fun getEnheterMedTemaerForSaksbehandler(ident: String): EnheterMedLovligeTemaer
    fun getSaksbehandlereIEnhet(enhetId: String): List<SaksbehandlerIdent>
}