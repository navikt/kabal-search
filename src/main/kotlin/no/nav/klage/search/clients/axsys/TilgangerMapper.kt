package no.nav.klage.search.clients.axsys

import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.domain.kodeverk.LovligeTemaer
import no.nav.klage.search.domain.kodeverk.Tema
import no.nav.klage.search.domain.saksbehandler.EnhetMedLovligeTemaer
import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeTemaer
import no.nav.klage.search.util.getLogger
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class TilgangerMapper(environment: Environment) {

    private val lovligeTemaerIKabal = LovligeTemaer.lovligeTemaer(environment)

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun mapTilgangerToEnheter(tilganger: Tilganger): List<Enhet> =
        tilganger.enheter.map { enhet -> Enhet(enhet.enhetId, enhet.navn) }

    fun mapTilgangerToEnheterMedLovligeTemaer(tilganger: Tilganger): EnheterMedLovligeTemaer {

        return EnheterMedLovligeTemaer(tilganger.enheter.map { enhet ->
            EnhetMedLovligeTemaer(
                enhet.enhetId,
                enhet.navn,
                enhet.temaer?.mapNotNull { mapTemaNavnToTema(it) }?.filter { lovligeTemaerIKabal.contains(it) }
                    ?: emptyList())
        })
    }

    private fun mapTemaNavnToTema(tema: String): Tema? =
        try {
            Tema.fromNavn(tema)
        } catch (e: Exception) {
            logger.warn("Unable to map Tema $tema. Ignoring and moving on", e)
            null
        }
}