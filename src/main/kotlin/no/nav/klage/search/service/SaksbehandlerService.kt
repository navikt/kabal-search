package no.nav.klage.search.service

import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeYtelser
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.repositories.SaksbehandlerRepository
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerRepository: SaksbehandlerRepository,
) {
    fun getEnheterMedYtelserForSaksbehandler(): EnheterMedLovligeYtelser =
        innloggetSaksbehandlerRepository.getEnheterMedYtelserForSaksbehandler()

    fun getNamesForSaksbehandlere(idents: Set<String>): Map<String, String> =
        saksbehandlerRepository.getNamesForSaksbehandlere(idents)


}
