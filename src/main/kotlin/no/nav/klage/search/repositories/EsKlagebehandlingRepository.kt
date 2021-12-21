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
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.action.support.master.AcknowledgedResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.core.CountRequest
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.CreateIndexResponse
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.common.unit.TimeValue
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
import java.util.concurrent.TimeUnit


class EsKlagebehandlingRepository(val client: RestHighLevelClient) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()

        private val mapper =
            ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
        const val SETTINGS_CONFIG = "/elasticsearch/settings.json"
        const val MAPPING_CONFIG = "/elasticsearch/mapping.json"
    }

    fun indexExists(): Boolean {
        val request = GetIndexRequest("klagebehandling")
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
            val request = CreateIndexRequest("klagebehandling")
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
        val request = DeleteIndexRequest("klagebehandling")
        val deleteIndexResponse: AcknowledgedResponse = client.indices().delete(request, RequestOptions.DEFAULT)
        logger.info("Deletion of ES index klagebehandling is acknowledged: ${deleteIndexResponse.isAcknowledged}")
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

    fun save(
        klagebehandling: EsKlagebehandling,
        refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE
    ) {
        try {
            val request = IndexRequest("klagebehandling")
            request.id(klagebehandling.id)
            //TODO: Make json from klagebehandling
            val jsonString = mapper.writeValueAsString(klagebehandling)
            println(jsonString)
            request.source(jsonString, XContentType.JSON)
            request.refreshPolicy = refreshPolicy
            //request.version(klagebehandling.modified.toEpochSecond(ZoneOffset.UTC))
            request.versionType(VersionType.INTERNAL)
            //request.opType(DocWriteRequest.OpType.INDEX)
            val indexResponse: IndexResponse = client.index(request, RequestOptions.DEFAULT)
            logSaveStatus(indexResponse)

        } catch (e: ElasticsearchException) {
            if (e.status() == RestStatus.CONFLICT) {
                logger.info("Conflict when saving to ES, ignoring and moving on..")
                logger.debug("Failed to save klagebehandling to ES: ${e.detailedMessage}", e)
            } else {
                logger.error("Failed to save klagebehandling to ES: ${e.detailedMessage}", e)
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
        logger.info("Refreshed index, totalshards: $totalShards, successfulshards: $successfulShards")
        if (failedShards > 0) {
            logger.warn("Failure during refresh of ES, totalshards: $totalShards, failedshards: $failedShards")
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

    fun saveAll(klagebehandlinger: List<EsKlagebehandling>) {
        save(klagebehandlinger)
    }

    fun search(
        searchSourceBuilder: SearchSourceBuilder,
        aggregationBuilders: List<AggregationBuilder>
    ): KlagebehandlingerSearchHits {
        val searchRequest = SearchRequest("klagebehandling")
        searchRequest.source(searchSourceBuilder)

        aggregationBuilders.forEach { searchSourceBuilder.aggregation(it) }
        val searchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
        //TODO: Log all the same here as elsewhere
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
        searchSourceBuilder.from(0)
        searchSourceBuilder.size(5)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        //searchSourceBuilder.sort(FieldSortBuilder("id").order(SortOrder.ASC));
        return search(searchSourceBuilder, aggregationBuilders)
    }

    fun count(baseQuery: QueryBuilder): Long {
        val countRequest = CountRequest("klagebehandling")
        countRequest.query(baseQuery)
        val countResponse = client
            .count(countRequest, RequestOptions.DEFAULT)
        //TODO: Log all the same here as elsewhere
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