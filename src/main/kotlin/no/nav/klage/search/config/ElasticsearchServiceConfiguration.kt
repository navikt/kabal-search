package no.nav.klage.search.config

import no.nav.klage.search.repositories.EsKlagebehandlingRepository
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.service.ElasticsearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration
@EnableElasticsearchRepositories(basePackageClasses = [EsKlagebehandlingRepository::class])
class ElasticsearchServiceConfiguration {

    @Bean
    fun elasticsearchService(
        elasticsearchRestTemplate: ElasticsearchRestTemplate,
        innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
        esKlagebehandlingRepository: EsKlagebehandlingRepository
    ): ElasticsearchService {
        return ElasticsearchService(
            elasticsearchRestTemplate,
            innloggetSaksbehandlerRepository,
            esKlagebehandlingRepository
        )
    }

}