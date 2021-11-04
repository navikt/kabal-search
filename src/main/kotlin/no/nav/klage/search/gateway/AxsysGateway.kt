package no.nav.klage.search.gateway

import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeTemaer

interface AxsysGateway {

    fun getEnheterMedTemaerForSaksbehandler(ident: String): EnheterMedLovligeTemaer
}