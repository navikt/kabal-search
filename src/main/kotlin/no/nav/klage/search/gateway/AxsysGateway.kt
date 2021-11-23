package no.nav.klage.search.gateway

import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeTemaer

interface AxsysGateway {

    fun getEnheterForSaksbehandler(ident: String): List<Enhet>
    fun getEnheterMedTemaerForSaksbehandler(ident: String): EnheterMedLovligeTemaer
}