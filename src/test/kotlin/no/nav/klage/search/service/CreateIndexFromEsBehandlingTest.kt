package no.nav.klage.search.service


import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.opensearch.client.Request
import org.opensearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
class CreateIndexFromEsBehandlingTest {

    companion object {
        @Container
        @JvmField
        val esContainer: TestOpenSearchContainer = TestOpenSearchContainer.instance
    }

    @Autowired
    lateinit var client: RestHighLevelClient

    @Autowired
    lateinit var service: ElasticsearchService

    @Test
    @Order(1)
    fun `es is running`() {
        assertThat(esContainer.isRunning).isTrue
    }

    @Test
    @Order(2)
    fun `index is created`() {
        val mappingResponse = client.lowLevelClient.performRequest(Request("GET", "/_all/_mapping"))
        val mapping: String = EntityUtils.toString(mappingResponse.entity)
        println(mapping)
    }

    @Test
    @Order(3)
    fun `recreating index works`() {
        service.recreateIndex()
        val mappingResponse = client.lowLevelClient.performRequest(Request("GET", "/_all/_mapping"))
        EntityUtils.toString(mappingResponse.entity)
    }

}