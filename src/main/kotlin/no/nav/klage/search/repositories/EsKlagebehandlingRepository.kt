package no.nav.klage.search.repositories


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.apache.lucene.search.TotalHits
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.action.support.broadcast.BroadcastResponse
import org.elasticsearch.action.support.master.AcknowledgedResponse
import org.elasticsearch.action.support.replication.ReplicationResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.core.CountRequest
import org.elasticsearch.client.core.CountResponse
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.CreateIndexResponse
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.VersionType
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.reindex.BulkByScrollResponse
import org.elasticsearch.index.reindex.DeleteByQueryRequest
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.Aggregations
import org.elasticsearch.search.builder.SearchSourceBuilder


class EsKlagebehandlingRepository(val client: RestHighLevelClient) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()

        private val mapper =
            ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
        const val SETTINGS_CONFIG = "/elasticsearch/settings.json"
        const val MAPPING_CONFIG = "/elasticsearch/mapping.json"
        const val KLAGEBEHANDLING_INDEX = "klagebehandling"
    }

    fun indexExists(): Boolean {
        val request = GetIndexRequest(KLAGEBEHANDLING_INDEX)
        return client.indices().exists(request, RequestOptions.DEFAULT)
    }

    fun recreateIndex() {
        deleteIndex()
        createIndex()
    }

    fun createIndex() {
        logger.info("Trying to initialize Elasticsearch")
        logger.info("Does klagebehandling exist in Elasticsearch?")

        if (!indexExists()) {
            logger.info("klagebehandling does not exist in Elasticsearch")
            val request = CreateIndexRequest(KLAGEBEHANDLING_INDEX)
            request.settings(settings())
            request.mapping(mapping())
            val createIndexResponse: CreateIndexResponse = client.indices().create(request, RequestOptions.DEFAULT)
            logger.info("Creation of ES index klagebehandling is acknowledged: ${createIndexResponse.isAcknowledged}")
        } else {
            logger.info("klagebehandling does exist in Elasticsearch")
        }
    }

    fun deleteIndex() {
        logger.info("Deleting index klagebehandling")
        val request = DeleteIndexRequest(KLAGEBEHANDLING_INDEX)
        val deleteIndexResponse: AcknowledgedResponse = client.indices().delete(request, RequestOptions.DEFAULT)
        logger.info("Deletion of ES index klagebehandling is acknowledged: ${deleteIndexResponse.isAcknowledged}")
    }

    fun refreshIndex() {
        try {
            val request = RefreshRequest(KLAGEBEHANDLING_INDEX)
            val refreshResponse = client.indices().refresh(request, RequestOptions.DEFAULT)
            logResponseShardInfo(refreshResponse)
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

    fun save(
        klagebehandling: EsKlagebehandling,
        refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE
    ) {
        try {
            val request = IndexRequest(KLAGEBEHANDLING_INDEX)
            request.id(klagebehandling.id)
            val jsonString = mapper.writeValueAsString(klagebehandling)
            request.source(jsonString, XContentType.JSON)
            request.refreshPolicy = refreshPolicy
            request.versionType(VersionType.INTERNAL)
            //request.version(klagebehandling.modified.toEpochSecond(ZoneOffset.UTC))
            //request.opType(DocWriteRequest.OpType.INDEX)
            val indexResponse: IndexResponse = client.index(request, RequestOptions.DEFAULT)
            logIndexResponse(indexResponse)

        } catch (e: ElasticsearchException) {
            if (e.status() == RestStatus.CONFLICT) {
                logger.info("Conflict when saving to ES, ignoring and moving on..")
                logger.debug("Failed to save klagebehandling to ES: ${e.detailedMessage}", e)
            } else {
                logger.error("Failed to save klagebehandling to ES: ${e.detailedMessage}", e)
            }
        }
    }

    fun deleteAll() {
        val request = DeleteByQueryRequest(KLAGEBEHANDLING_INDEX)
        request.setQuery(QueryBuilders.matchAllQuery())
        val response: BulkByScrollResponse = client.deleteByQuery(request, RequestOptions.DEFAULT)
        logBulkResponse(response)
    }

    fun saveAll(klagebehandlinger: List<EsKlagebehandling>) {
        save(klagebehandlinger)
    }

    fun search(
        searchSourceBuilder: SearchSourceBuilder,
        aggregationBuilders: List<AggregationBuilder>
    ): KlagebehandlingerSearchHits {
        val searchRequest = SearchRequest(KLAGEBEHANDLING_INDEX)
        searchRequest.source(searchSourceBuilder)

        aggregationBuilders.forEach { searchSourceBuilder.aggregation(it) }
        val searchResponse: SearchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
        logSearchResponseShardInfo(searchResponse)
        return KlagebehandlingerSearchHits(
            totalHits = searchResponse.hits.totalHits!!.value,
            totalHitsRelation = searchResponse.hits.totalHits!!.relation,
            searchHits = searchResponse.hits.map {
                EsKlagebehandlingSearchHit(
                    mapper.readValue(
                        it.sourceAsString,
                        EsKlagebehandling::class.java
                    )
                )
            },
            aggregations = searchResponse.aggregations
        )
    }

    fun search(
        queryBuilder: QueryBuilder,
        aggregationBuilders: List<AggregationBuilder> = emptyList()
    ): KlagebehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(queryBuilder)
        return search(searchSourceBuilder, aggregationBuilders)
    }

    fun count(baseQuery: QueryBuilder): Long {
        val countRequest = CountRequest(KLAGEBEHANDLING_INDEX)
        countRequest.query(baseQuery)
        val countResponse: CountResponse = client.count(countRequest, RequestOptions.DEFAULT)
        logCountResponseShardInfo(countResponse)
        return countResponse.count
    }

    private fun settings(): Map<String, Any> {
        val parser = XContentType.JSON.xContent().createParser(
            NamedXContentRegistry.EMPTY, null,
            EsKlagebehandlingRepository::class.java.getResourceAsStream(SETTINGS_CONFIG)
        )
        return parser.map()
    }

    private fun mapping(): Map<String, Any> {
        val parser = XContentType.JSON.xContent().createParser(
            NamedXContentRegistry.EMPTY, null,
            EsKlagebehandlingRepository::class.java.getResourceAsStream(MAPPING_CONFIG)
        )
        return parser.map()
    }

    private fun logIndexResponse(indexResponse: IndexResponse) {
        val index = indexResponse.index
        val id = indexResponse.id
        if (indexResponse.result == DocWriteResponse.Result.CREATED) {
            logger.info("Created klagebehandling in ES index $index with id $id")
        } else if (indexResponse.result == DocWriteResponse.Result.UPDATED) {
            logger.info("Updated klagebehandling in ES index $index with id $id")
        }
        logIndexResponseShardInfo(indexResponse.shardInfo)
    }

    private fun logBulkResponse(response: BulkByScrollResponse) {
        if (response.bulkFailures.size > 0) {
            logger.warn("Failures in bulk response")
            response.bulkFailures.forEach {
                logger.warn("Details about failure; status: ${it.status}, message: ${it.message}", it.cause)
            }
        }
    }

    private fun logSearchResponseShardInfo(response: SearchResponse) {
        val totalShards = response.totalShards
        val successfulShards = response.successfulShards
        val failedShards = response.failedShards
        val failures = response.shardFailures
        logger.debug("Response result; totalshards: $totalShards, successfulshards: $successfulShards")
        if (failedShards > 0) {
            logger.warn("Failures in response result, totalshards: $totalShards, failedshards: $failedShards")
            for (failure in failures) {
                val reason = failure.reason()
                logger.warn("Reason for failure: $reason")
            }
        }
    }

    private fun logIndexResponseShardInfo(shardInfo: ReplicationResponse.ShardInfo) {
        val totalShards = shardInfo.total
        val successfulShards = shardInfo.successful
        val failedShards = shardInfo.failed
        val failures = shardInfo.failures
        logger.debug("Response result; totalshards: $totalShards, successfulshards: $successfulShards")
        if (failedShards > 0) {
            logger.warn("Failures in response result, totalshards: $totalShards, failedshards: $failedShards")
            for (failure in failures) {
                val reason = failure.reason()
                logger.warn("Reason for failure: $reason")
            }
        }
    }

    private fun logCountResponseShardInfo(response: CountResponse) {
        val totalShards = response.totalShards
        val successfulShards = response.successfulShards
        val failedShards = response.failedShards
        val failures = response.shardFailures
        logger.debug("Response result; totalshards: $totalShards, successfulshards: $successfulShards")
        if (failedShards > 0) {
            logger.warn("Failures in response result, totalshards: $totalShards, failedshards: $failedShards")
            for (failure in failures) {
                val reason = failure.reason()
                logger.warn("Reason for failure: $reason")
            }
        }
    }

    private fun logResponseShardInfo(response: BroadcastResponse) {
        val totalShards = response.totalShards
        val successfulShards = response.successfulShards
        val failedShards = response.failedShards
        val failures = response.shardFailures
        logger.debug("Response result; totalshards: $totalShards, successfulshards: $successfulShards")
        if (failedShards > 0) {
            logger.warn("Failures in response result, totalshards: $totalShards, failedshards: $failedShards")
            failures.forEach { logger.warn("Reason for failure: ${it.reason()}") }
        }
    }
}

interface SearchHits<T> : Iterable<SearchHit<T>> {

    val aggregations: Aggregations?

    val searchHits: List<SearchHit<T>>

    val totalHits: Long

    val totalHitsRelation: TotalHits.Relation

    fun hasAggregations(): Boolean {
        return aggregations != null
    }

    fun hasSearchHits(): Boolean {
        return searchHits.isNotEmpty()
    }

    override fun iterator(): Iterator<SearchHit<T>> {
        return searchHits.iterator()
    }
}

open class SearchHit<T>(val id: String, val content: T)

class EsKlagebehandlingSearchHit(content: EsKlagebehandling) :
    SearchHit<EsKlagebehandling>(content.id, content)


class KlagebehandlingerSearchHits(
    override val totalHits: Long,
    override val totalHitsRelation: TotalHits.Relation,
    override val searchHits: List<SearchHit<EsKlagebehandling>>,
    override val aggregations: Aggregations?
) : SearchHits<EsKlagebehandling>