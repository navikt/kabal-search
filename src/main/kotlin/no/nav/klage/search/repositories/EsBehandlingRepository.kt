package no.nav.klage.search.repositories


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.search.domain.elasticsearch.EsAnonymBehandling
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.apache.lucene.search.TotalHits
import org.opensearch.OpenSearchException
import org.opensearch.action.DocWriteResponse
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest
import org.opensearch.action.admin.indices.refresh.RefreshRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.action.index.IndexResponse
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.search.SearchResponse
import org.opensearch.action.support.WriteRequest
import org.opensearch.action.support.broadcast.BroadcastResponse
import org.opensearch.action.support.master.AcknowledgedResponse
import org.opensearch.action.support.replication.ReplicationResponse
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.core.CountRequest
import org.opensearch.client.core.CountResponse
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.CreateIndexResponse
import org.opensearch.client.indices.GetIndexRequest
import org.opensearch.common.xcontent.XContentType
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.index.VersionType
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.index.reindex.BulkByScrollResponse
import org.opensearch.index.reindex.DeleteByQueryRequest
import org.opensearch.rest.RestStatus
import org.opensearch.search.aggregations.AggregationBuilder
import org.opensearch.search.aggregations.Aggregations
import org.opensearch.search.builder.SearchSourceBuilder
import java.util.*


class EsBehandlingRepository(val client: RestHighLevelClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()

        private val mapper =
            ObjectMapper().registerModule(
                KotlinModule.Builder()
                    .withReflectionCacheSize(512)
                    .configure(KotlinFeature.NullToEmptyCollection, false)
                    .configure(KotlinFeature.NullToEmptyMap, false)
                    .configure(KotlinFeature.NullIsSameAsDefault, false)
                    .configure(KotlinFeature.SingletonSupport, false)
                    .configure(KotlinFeature.StrictNullChecks, false)
                    .
                    build()
            ).registerModule(JavaTimeModule())
        const val SETTINGS_CONFIG = "/elasticsearch/settings.json"
        const val MAPPING_CONFIG = "/elasticsearch/mapping.json"
        const val BEHANDLING_INDEX = "klagebehandling"
    }

    fun indexExists(): Boolean {
        val request = GetIndexRequest(BEHANDLING_INDEX)
        return client.indices().exists(request, RequestOptions.DEFAULT)
    }

    fun recreateIndex() {
        deleteIndex()
        createIndex()
    }

    fun createIndex() {
        logger.info("Trying to initialize Elasticsearch")
        logger.info("Does $BEHANDLING_INDEX exist in Elasticsearch?")

        if (!indexExists()) {
            logger.info("$BEHANDLING_INDEX does not exist in Elasticsearch")
            val request = CreateIndexRequest(BEHANDLING_INDEX)
            request.settings(settings())
            request.mapping(mapping())
            val createIndexResponse: CreateIndexResponse = client.indices().create(request, RequestOptions.DEFAULT)
            logger.info("Creation of ES index $BEHANDLING_INDEX is acknowledged: ${createIndexResponse.isAcknowledged}")
        } else {
            logger.info("$BEHANDLING_INDEX does exist in Elasticsearch")
        }
    }

    fun deleteIndex() {
        logger.info("Deleting index $BEHANDLING_INDEX")
        val request = DeleteIndexRequest(BEHANDLING_INDEX)
        val deleteIndexResponse: AcknowledgedResponse = client.indices().delete(request, RequestOptions.DEFAULT)
        logger.info("Deletion of ES index $BEHANDLING_INDEX is acknowledged: ${deleteIndexResponse.isAcknowledged}")
    }

    fun refreshIndex() {
        try {
            val request = RefreshRequest(BEHANDLING_INDEX)
            val refreshResponse = client.indices().refresh(request, RequestOptions.DEFAULT)
            logResponseShardInfo(refreshResponse)
        } catch (exception: OpenSearchException) {
            if (exception.status() === RestStatus.NOT_FOUND) {
                logger.error("Unable to refresh ES index, index not found")
            }
        }
    }

    fun save(behandlinger: List<EsBehandling>) {
        //TODO Kunne kanskje med fordel vært håndtert med en BulkRequest, ref https://github.com/navikt/pam-kandidatsok-es/blob/master/src/main/java/no/nav/arbeid/kandidatsok/es/client/EsIndexerHttpService.java
        behandlinger.forEach {
            save(it, WriteRequest.RefreshPolicy.NONE)
        }
        refreshIndex()
    }

    fun save(
        behandling: EsBehandling,
        refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE
    ) {
        try {
            val request = IndexRequest(BEHANDLING_INDEX)
            request.id(behandling.behandlingId)
            val jsonString = mapper.writeValueAsString(behandling)
            request.source(jsonString, XContentType.JSON)
            request.refreshPolicy = refreshPolicy
            request.versionType(VersionType.INTERNAL)
            //request.version(klagebehandling.modified.toEpochSecond(ZoneOffset.UTC))
            //request.opType(DocWriteRequest.OpType.INDEX)
            val indexResponse: IndexResponse = client.index(request, RequestOptions.DEFAULT)
            logIndexResponse(indexResponse)

        } catch (e: OpenSearchException) {
            if (e.status() == RestStatus.CONFLICT) {
                logger.info("Conflict when saving to ES, ignoring and moving on..")
                logger.debug("Failed to save behandling to ES: ${e.detailedMessage}", e)
            } else {
                logger.error("Failed to save behandling to ES: ${e.detailedMessage}", e)
            }
        }
    }

    fun deleteAll() {
        val request = DeleteByQueryRequest(BEHANDLING_INDEX)
        request.setQuery(QueryBuilders.matchAllQuery())
        val response: BulkByScrollResponse = client.deleteByQuery(request, RequestOptions.DEFAULT)
        logBulkResponse(response)
    }

    fun deleteBehandling(behandlingId: UUID) {
        val request = DeleteByQueryRequest(BEHANDLING_INDEX)
        request.setQuery(QueryBuilders.idsQuery().addIds(behandlingId.toString()))
        val response: BulkByScrollResponse = client.deleteByQuery(request, RequestOptions.DEFAULT)
        logBulkResponse(response)
    }

    fun saveAll(behandlinger: List<EsBehandling>) {
        save(behandlinger)
    }

    fun search(
        searchSourceBuilder: SearchSourceBuilder,
        aggregationBuilders: List<AggregationBuilder> = emptyList(),
    ): BehandlingerSearchHits {
        val searchRequest = SearchRequest(BEHANDLING_INDEX)
        searchRequest.source(searchSourceBuilder)

        aggregationBuilders.forEach { searchSourceBuilder.aggregation(it) }
        val searchResponse: SearchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
        logSearchResponseShardInfo(searchResponse)
        return BehandlingerSearchHits(
            totalHits = searchResponse.hits.totalHits!!.value,
            totalHitsRelation = searchResponse.hits.totalHits!!.relation,
            searchHits = searchResponse.hits.map {
                EsBehandlingSearchHit(
                    mapper.readValue(
                        it.sourceAsString,
                        EsBehandling::class.java
                    )
                )
            },
            aggregations = searchResponse.aggregations
        )
    }

    fun search(
        queryBuilder: QueryBuilder,
        aggregationBuilders: List<AggregationBuilder> = emptyList()
    ): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(queryBuilder)
        return search(searchSourceBuilder, aggregationBuilders)
    }

    fun count(baseQuery: QueryBuilder): Long {
        val countRequest = CountRequest(BEHANDLING_INDEX)
        countRequest.query(baseQuery)
        val countResponse: CountResponse = client.count(countRequest, RequestOptions.DEFAULT)
        logCountResponseShardInfo(countResponse)
        return countResponse.count
    }

    private fun settings(): Map<String, Any> {
        val parser = XContentType.JSON.xContent().createParser(
            NamedXContentRegistry.EMPTY, null,
            EsBehandlingRepository::class.java.getResourceAsStream(SETTINGS_CONFIG)
        )
        return parser.map()
    }

    private fun mapping(): Map<String, Any> {
        val parser = XContentType.JSON.xContent().createParser(
            NamedXContentRegistry.EMPTY, null,
            EsBehandlingRepository::class.java.getResourceAsStream(MAPPING_CONFIG)
        )
        return parser.map()
    }

    private fun logIndexResponse(indexResponse: IndexResponse) {
        val index = indexResponse.index
        val id = indexResponse.id
        if (indexResponse.result == DocWriteResponse.Result.CREATED) {
            logger.info("Created behandling in ES index $index with id $id")
        } else if (indexResponse.result == DocWriteResponse.Result.UPDATED) {
            logger.info("Updated behandling in ES index $index with id $id")
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

class EsBehandlingSearchHit(content: EsBehandling) :
    SearchHit<EsBehandling>(content.behandlingId, content)

class EsAnonymBehandlingSearchHit(content: EsBehandling) :
    SearchHit<EsAnonymBehandling>(content.behandlingId, content)


class BehandlingerSearchHits(
    override val totalHits: Long,
    override val totalHitsRelation: TotalHits.Relation,
    override val searchHits: List<SearchHit<EsBehandling>>,
    override val aggregations: Aggregations?
) : SearchHits<EsBehandling> {
    fun anonymize(): AnonymeBehandlingerSearchHits {
        return AnonymeBehandlingerSearchHits(
            totalHits = totalHits,
            totalHitsRelation = totalHitsRelation,
            searchHits = searchHits.map { EsAnonymBehandlingSearchHit(it.content) },
            aggregations = aggregations,
        )
    }
}

class AnonymeBehandlingerSearchHits(
    override val totalHits: Long,
    override val totalHitsRelation: TotalHits.Relation,
    override val searchHits: List<SearchHit<EsAnonymBehandling>>,
    override val aggregations: Aggregations?
) : SearchHits<EsAnonymBehandling>