package no.nav.klage.search.service

import no.nav.klage.search.clients.kabalapi.KlagebehandlingSkjemaV1
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

    /*

    fun reindexAllKlagebehandlinger() {
        var pageable: Pageable =
            PageRequest.of(0, 50, Sort.by("created").descending())
        do {
            val page = klagebehandlingRepository.findAll(pageable)
            page.content.map { klagebehandling ->
                try {
                    esKlagebehandlingMapper.mapKlagebehandlingOgMottakToEsKlagebehandling(klagebehandling)
                        .let { elasticsearchService.save(it) }
                } catch (e: Exception) {
                    logger.warn("Exception during indexing", e)
                }
            }
            pageable = page.nextPageable()
        } while (pageable.isPaged)
    }

    fun findAndLogOutOfSyncKlagebehandlinger() {
        val esData = elasticsearchService.findAllIdAndModified()
        val dbData = idAndModifiedInDb()
        logger.info("Number of klagebehandlinger in ES: ${esData.size}, number of klagebehandlinger in DB: ${dbData.size}")
        logger.info(
            "Klagebehandlinger in ES that are not in DB: {}",
            esData.keys.minus(dbData.keys)
        )
        logger.info(
            "Klagebehandlinger in DB that are not in ES: {}",
            dbData.keys.minus(esData.keys)
        )
        dbData.keys.forEach { id ->
            val dbValue = dbData.getValue(id)
            val esValue = esData[id]
            if (esValue != null) {
                //If esValue == null it will already have been logged as "Klagebehandlinger in DB that are not in ES"
                if (!dbValue.truncatedTo(ChronoUnit.MILLIS).isEqual(esValue.truncatedTo(ChronoUnit.MILLIS))) {
                    logger.info(
                        "Klagebehandling {} is not up-to-date in ES, modified is {} in DB and {} in ES",
                        id,
                        dbValue,
                        esValue
                    )
                }
            }
        }
    }

    private fun idAndModifiedInDb(): Map<String, LocalDateTime> {
        val idsInDb = mutableListOf<Pair<String, LocalDateTime>>()
        var pageable: Pageable =
            PageRequest.of(0, 50)
        do {
            val page = klagebehandlingRepository.findAll(pageable)
            page.content.map { it.id.toString() to it.modified }.let { idsInDb.addAll(it) }
            pageable = page.nextPageable()
        } while (pageable.isPaged)
        return idsInDb.toMap()
    }
    */


    @Retryable
    fun indexKlagebehandling(klagebehandling: KlagebehandlingSkjemaV1) {
        try {
            elasticsearchService.save(
                esKlagebehandlingMapper.mapKlagebehandlingOgMottakToEsKlagebehandling(klagebehandling)
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