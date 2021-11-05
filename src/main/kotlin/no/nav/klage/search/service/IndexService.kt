package no.nav.klage.search.service

import no.nav.klage.search.clients.klageendret.KlagebehandlingSkjemaV1
import no.nav.klage.search.service.mapper.EsKlagebehandlingMapper
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class IndexService(
    private val elasticsearchService: ElasticsearchService,
    private val esKlagebehandlingMapper: EsKlagebehandlingMapper
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun deleteAllKlagebehandlinger() {
        elasticsearchService.deleteAll()
    }

    @Retryable
    fun indexKlagebehandling(klagebehandling: KlagebehandlingSkjemaV1) {
        logger.debug("Skal indeksere fra kabal-search, klage med id ${klagebehandling.id}")
        try {
            elasticsearchService.save(
                esKlagebehandlingMapper.mapKlagebehandlingToEsKlagebehandling(klagebehandling)
            )
        } catch (e: Exception) {
            if (e.message?.contains("version_conflict_engine_exception") == true) {
                logger.info("Later version already indexed, ignoring this..")
            } else {
                logger.error("Unable to index klagebehandling ${klagebehandling.id}, see securelogs for details")
                securelogger.error("Unable to index klagebehandling ${klagebehandling.id}", e)
            }
        }
    }

    fun recreateIndex() {
        elasticsearchService.recreateIndex()
    }

}