package no.nav.klage.search.config

import no.finn.unleash.Unleash
import no.nav.klage.search.repositories.EsKlagebehandlingRepository
import no.nav.klage.search.service.ElasticsearchService
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


@Configuration
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
            RestClient.builder(HttpHost(hostname, port, scheme))
                .setRequestConfigCallback {
                    it.setConnectionRequestTimeout(5000).setConnectTimeout(10000).setSocketTimeout(20000)
                }
                .setHttpClientConfigCallback { it.setDefaultCredentialsProvider(credentialsProvider) }
        )
    }

    @Bean
    fun esKlagebehandlingRepository(): EsKlagebehandlingRepository {
        return EsKlagebehandlingRepository(restHighLevelClient())
    }

    @Bean
    fun elasticsearchService(
        unleash: Unleash
    ): ElasticsearchService {
        return ElasticsearchService(
            esKlagebehandlingRepository(),
            unleash
        )
    }


}