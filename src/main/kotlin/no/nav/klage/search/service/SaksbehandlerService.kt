package no.nav.klage.search.service

import no.nav.klage.search.domain.kodeverk.Tema
import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeTemaer
import no.nav.klage.search.gateway.AzureGateway
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.repositories.SaksbehandlerRepository
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val tilgangService: TilgangService,
    private val azureGateway: AzureGateway
) {
    fun getEnheterMedTemaerForSaksbehandler(): EnheterMedLovligeTemaer =
        innloggetSaksbehandlerRepository.getEnheterMedTemaerForSaksbehandler()

    private fun saksbehandlerHarTilgangTilPerson(ident: String, fnr: String): Boolean =
        tilgangService.harSaksbehandlerTilgangTil(ident, fnr)


    private fun saksbehandlerHarTilgangTilTema(ident: String, tema: Tema) =
        saksbehandlerRepository.getEnheterMedTemaerForSaksbehandler(ident).enheter.flatMap { it.temaer }
            .contains(tema)

    fun getNameForIdent(it: String) =
        saksbehandlerRepository.getNamesForSaksbehandlere(setOf(it)).getOrDefault(it, "Ukjent navn")

    fun getNamesForSaksbehandlere(idents: Set<String>): Map<String, String> =
        saksbehandlerRepository.getNamesForSaksbehandlere(idents)


}
