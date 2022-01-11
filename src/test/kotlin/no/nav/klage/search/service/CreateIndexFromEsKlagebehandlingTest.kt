package no.nav.klage.search.service


import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.finn.unleash.Unleash
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import org.apache.http.util.EntityUtils
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
class CreateIndexFromEsKlagebehandlingTest {

    companion object {
        @Container
        @JvmField
        val esContainer: TestOpenSearchContainer = TestOpenSearchContainer.instance
    }

    @MockkBean(relaxed = true)
    lateinit var unleash: Unleash

    @MockkBean(relaxed = true)
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @Autowired
    lateinit var client: RestHighLevelClient

    @Autowired
    lateinit var service: ElasticsearchService

    @BeforeEach
    fun setup() {
        every { unleash.isEnabled(any(), false) } returns true
    }

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

    /*
    @Test
    @Order(3)
    fun `denne vil printe ut mapping generert fra EsKlagebehandling`() {
        val indexOps = esTemplate.indexOps(EsKlagebehandling::class.java)
        val mappingDocument = indexOps.createMapping(EsKlagebehandling::class.java)
        println(mappingDocument.toJson())
    }
    */

}