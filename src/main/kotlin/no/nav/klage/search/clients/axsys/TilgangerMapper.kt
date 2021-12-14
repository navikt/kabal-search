package no.nav.klage.search.clients.axsys

import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Component

@Component
class TilgangerMapper {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun mapTilgangerToEnheter(tilganger: Tilganger): List<Enhet> =
        tilganger.enheter.map { enhet -> Enhet(enhet.enhetId, enhet.navn) }
}