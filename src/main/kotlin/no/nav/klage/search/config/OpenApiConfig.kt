package no.nav.klage.search.config

import no.nav.klage.search.api.controller.OppgaverListController
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun apiInternal(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .packagesToScan(OppgaverListController::class.java.packageName)
            .group("standard")
            .pathsToMatch("/**")
            .build()
    }

}
