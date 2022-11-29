package no.nav.klage.search.service

import no.nav.klage.search.clients.klageendret.BehandlingSkjemaV2
import no.nav.klage.search.clients.klageendret.KlagebehandlingSkjemaV1
import no.nav.klage.search.service.mapper.EsBehandlingMapper
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import java.util.*

@Service
class IndexService(
    private val elasticsearchService: ElasticsearchService,
    private val esBehandlingMapper: EsBehandlingMapper
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun deleteAllBehandlinger() {
        elasticsearchService.deleteAll()
    }

    fun recreateIndex() {
        elasticsearchService.recreateIndex()
    }

    @Retryable
    fun indexBehandling(behandling: BehandlingSkjemaV2) {
        try {
            elasticsearchService.save(
                esBehandlingMapper.mapBehandlingToEsBehandling(behandling)
            )
        } catch (e: Exception) {
            if (e.message?.contains("version_conflict_engine_exception") == true) {
                logger.info("Later version already indexed, ignoring this..")
            } else {
                logger.error("Unable to index behandling ${behandling.id}, see securelogs for details")
                securelogger.error("Unable to index behandling ${behandling.id}", e)
                throw e
            }
        }
    }

    fun deleteBehandling(behandlingId: UUID) {
        elasticsearchService.deleteBehandling(behandlingId)
    }

}