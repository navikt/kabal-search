package no.nav.klage.search.config

//import no.nav.klage.search.repositories.EsKlagebehandlingRepository
import no.finn.unleash.Unleash
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.elasticsearch.CreateIndexService
import no.nav.klage.search.service.elasticsearch.EsKlagebehandlingRepository
import no.nav.klage.search.service.elasticsearch.SaveService
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextStoppedEvent
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate


@Configuration
//@EnableElasticsearchRepositories(basePackageClasses = [EsKlagebehandlingRepository::class])
class ElasticsearchServiceConfiguration(
    @Value("\${AIVEN_ES_SCHEME}") private val scheme: String,
    @Value("\${AIVEN_ES_HOST}") private val hostname: String,
    @Value("\${AIVEN_ES_PORT}") private val port: Int,
    @Value("\${ELASTIC_USERNAME}") private val username: String,
    @Value("\${ELASTIC_PASSWORD}") private val password: String,
) :
    ApplicationListener<ContextStoppedEvent> {

    override fun onApplicationEvent(event: ContextStoppedEvent) {
        restHighLevelClient().close()
    }

    @Bean
    fun restHighLevelClient(): RestHighLevelClient {
        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(username, password))

        return RestHighLevelClient(
            RestClient.builder(
                HttpHost(hostname, port, scheme)
            ).setHttpClientConfigCallback { it.setDefaultCredentialsProvider(credentialsProvider) }
        )
    }

    @Bean
    fun createIndexService(): CreateIndexService {
        return CreateIndexService(restHighLevelClient())
    }

    @Bean
    fun saveService(): SaveService {
        return SaveService(restHighLevelClient())
    }

    @Bean
    fun esKlagebehandlingRepository(): EsKlagebehandlingRepository {
        return EsKlagebehandlingRepository(createIndexService(), saveService())
    }

    @Bean
    fun elasticsearchService(
        elasticsearchRestTemplate: ElasticsearchRestTemplate,
        innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
        //esKlagebehandlingRepository: EsKlagebehandlingRepository,
        unleash: Unleash
    ): ElasticsearchService {
        return ElasticsearchService(
            elasticsearchRestTemplate,
            innloggetSaksbehandlerRepository,
            createIndexService(),
            saveService(),
            unleash
        )
    }


}