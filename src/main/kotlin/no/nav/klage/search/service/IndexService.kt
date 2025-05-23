package no.nav.klage.search.service

import no.nav.klage.search.clients.klageendret.BehandlingSkjemaV2
import no.nav.klage.search.service.mapper.EsBehandlingMapper
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getTeamLogger
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
        private val teamLogger = getTeamLogger()
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
                logger.error("Unable to index behandling ${behandling.id}, see team-logs for details")
                teamLogger.error("Unable to index behandling ${behandling.id}", e)
                throw RuntimeException("Unable to index behandling ${behandling.id}")
            }
        }
    }

    fun deleteBehandling(behandlingId: UUID) {
        elasticsearchService.deleteBehandling(behandlingId)
    }

}