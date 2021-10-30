package no.nav.klage.search.config

import no.nav.klage.search.api.controller.KlagebehandlingListController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.Tag
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
class OpenApiConfig {

    @Bean
    fun apiInternal(): Docket {
        return Docket(DocumentationType.OAS_30)
            .select()
            .apis(RequestHandlerSelectors.basePackage(KlagebehandlingListController::class.java.packageName))
            .build()
            .pathMapping("/")
            .groupName("internal")
            .genericModelSubstitutes(ResponseEntity::class.java)
            .tags(Tag("kabal-search", "API for søk for saksbehandlere ved klageinstansen"))
    }

}
