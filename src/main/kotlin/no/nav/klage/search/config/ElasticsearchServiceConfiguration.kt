package no.nav.klage.search.config

import no.nav.klage.search.repositories.EsBehandlingRepository
import no.nav.klage.search.service.ElasticsearchService
import org.apache.hc.client5.http.auth.AuthScope
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.util.Timeout
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextStoppedEvent


@Configuration
class ElasticsearchServiceConfiguration(
    @Value("\${OPEN_SEARCH_USERNAME}") private val username: String,
    @Value("\${OPEN_SEARCH_PASSWORD}") private val password: String,
    @Value("\${OPEN_SEARCH_URI}") private val uri: String,
) :
    ApplicationListener<ContextStoppedEvent> {

    override fun onApplicationEvent(event: ContextStoppedEvent) {
        restHighLevelClient().close()
    }

    @Bean
    fun restHighLevelClient(): RestHighLevelClient {
        val httpHost = HttpHost.create(uri)

        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope(httpHost, null, null),
            UsernamePasswordCredentials(username, password.toCharArray())
        )

        return RestHighLevelClient(
            RestClient.builder(httpHost)
                .setRequestConfigCallback {
                    it.setConnectionRequestTimeout(Timeout.ofMilliseconds(5000))
                        .setConnectTimeout(Timeout.ofMilliseconds(10000))
                }
                .setHttpClientConfigCallback { it.setDefaultCredentialsProvider(credentialsProvider) }
        )
    }

    @Bean
    fun esKlagebehandlingRepository(): EsBehandlingRepository {
        return EsBehandlingRepository(restHighLevelClient())
    }

    @Bean
    fun elasticsearchService(): ElasticsearchService {
        return ElasticsearchService(esKlagebehandlingRepository())
    }


}