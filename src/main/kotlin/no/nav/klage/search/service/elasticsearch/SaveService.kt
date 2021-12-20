package no.nav.klage.search.service.elasticsearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.index.VersionType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.reindex.BulkByScrollResponse
import org.elasticsearch.index.reindex.DeleteByQueryRequest
import org.elasticsearch.rest.RestStatus
import java.time.ZoneOffset


class SaveService(
    private val client: RestHighLevelClient,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
        private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

    fun refreshIndex() {
        try {
            val request = RefreshRequest("klagebehandling")
            val refreshResponse = client.indices().refresh(request, RequestOptions.DEFAULT)
            logRefreshStatus(refreshResponse)
        } catch (exception: ElasticsearchException) {
            if (exception.status() === RestStatus.NOT_FOUND) {
                logger.error("Unable to refresh ES index, index not found")
            }
        }
    }

    fun save(klagebehandlinger: List<EsKlagebehandling>) {
        klagebehandlinger.forEach {
            save(it, WriteRequest.RefreshPolicy.NONE)
        }
        refreshIndex()
    }

    private fun createJson(klagebehandling: EsKlagebehandling): XContentBuilder {
        val builder = XContentFactory.jsonBuilder()
        builder.startObject()
        builder.field("_source", mapper.writeValueAsString(klagebehandling))
        builder.endObject()
        return builder
    }

    fun save(
        klagebehandling: EsKlagebehandling,
        refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE
    ) {
        try {
            val request = IndexRequest("klagebehandling")
            request.id(klagebehandling.id)
            //TODO: Make json from klagebehandling
            val jsonString = mapper.writeValueAsString(klagebehandling)
            request.source(createJson(klagebehandling))
            request.request.refreshPolicy = refreshPolicy
            request.version(klagebehandling.modified.toEpochSecond(ZoneOffset.UTC))
            request.versionType(VersionType.EXTERNAL)
            //request.opType(DocWriteRequest.OpType.INDEX)
            val indexResponse: IndexResponse = client.index(request, RequestOptions.DEFAULT)
            logSaveStatus(indexResponse)

        } catch (e: ElasticsearchException) {
            if (e.status() == RestStatus.CONFLICT) {
                logger.info("Conflict when saving to ES, ignoring and moving on..")
            } else {
                logger.error("Failed to save klagebehandling to ES", e)
            }
        }
    }

    private fun logSaveStatus(indexResponse: IndexResponse) {
        val index = indexResponse.index
        val id = indexResponse.id
        if (indexResponse.result == DocWriteResponse.Result.CREATED) {
            logger.info("Created klagebehandling in ES index $index with id $id")
        } else if (indexResponse.result == DocWriteResponse.Result.UPDATED) {
            logger.info("Updated klagebehandling in ES index $index with id $id")
        }

        val shardInfo = indexResponse.shardInfo
        logger.info("Refreshed index, totalshards: ${shardInfo.total}, successfulshards: ${shardInfo.successful}")

        if (shardInfo.failed > 0) {
            logger.warn("Failure during save to ES; reason: , totalshards: ${shardInfo.total}, failedshards: ${shardInfo.failed}")
            for (failure in shardInfo.failures) {
                val reason = failure.reason()
                logger.warn("Reason for failure: $reason")
            }
        }
    }

    private fun logRefreshStatus(refreshResponse: RefreshResponse) {
        val totalShards = refreshResponse.totalShards
        val successfulShards = refreshResponse.successfulShards
        val failedShards = refreshResponse.failedShards
        val failures = refreshResponse.shardFailures
        logger.info("Refreshed index, totalshards: ${totalShards}, successfulshards: ${successfulShards}")
        if (failedShards > 0) {
            logger.warn("Failure during refresh of ES, totalshards: ${totalShards}, failedshards: ${failedShards}")
            for (failure in failures) {
                val reason = failure.reason()
                logger.warn("Reason for failure: $reason")
            }
        }
    }

    fun deleteAll() {
        val request = DeleteByQueryRequest("klagebehandling")
        request.setQuery(QueryBuilders.matchAllQuery())
        val response: BulkByScrollResponse = client.deleteByQuery(request, RequestOptions.DEFAULT)
        //TODO: Log response?
    }
}