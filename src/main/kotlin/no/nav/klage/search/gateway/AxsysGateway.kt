package no.nav.klage.search.gateway

import no.nav.klage.search.domain.saksbehandler.Enhet

interface AxsysGateway {

    fun getEnheterForSaksbehandler(ident: String): List<Enhet>
}