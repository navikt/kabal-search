package no.nav.klage.search.service.elasticsearch

import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.support.master.AcknowledgedResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.CreateIndexResponse
import org.elasticsearch.client.indices.GetIndexRequest
import org.springframework.core.io.ClassPathResource
import org.springframework.data.elasticsearch.core.document.Document


class CreateIndexService(val client: RestHighLevelClient)
//    : ApplicationListener<ContextRefreshedEvent>
{
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    /*
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        try {
            createIndex()
        } catch (e: Exception) {
            logger.error("Unable to initialize Elasticsearch", e)
        }
    }
    */

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
            request.settings(readFromfile("settings.json"))
            request.mapping(readFromfile("mapping.json"))
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

    private fun readFromfile(filename: String): Document {
        val text: String =
            ClassPathResource("elasticsearch/${filename}").inputStream.bufferedReader(Charsets.UTF_8).readText()
        return Document.parse(text)
    }
}