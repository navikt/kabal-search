package no.nav.klage.search

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.Architectures.layeredArchitecture


@AnalyzeClasses(packages = ["no.nav.klage.search"], importOptions = [ImportOption.DoNotIncludeTests::class])
class LayeredArchitectureTest {

    fun kabalApiLayeredArchitecture() = layeredArchitecture().consideringAllDependencies()
        .layer("Controllers").definedBy("no.nav.klage.search.api.controller")
        .layer("ApiMappers").definedBy("no.nav.klage.search.api.mapper")
        .layer("View").definedBy("no.nav.klage.search.api.view")
        .layer("Services").definedBy("no.nav.klage.search.service..")
        .layer("Repositories").definedBy("no.nav.klage.search.repositories..")
        .layer("Clients").definedBy("no.nav.klage.search.clients..")
        .layer("Config").definedBy("no.nav.klage.search.config..")
        .layer("Domain").definedBy("no.nav.klage.search.domain..")
        .layer("Util").definedBy("no.nav.klage.search.util..")
        .layer("Exceptions").definedBy("no.nav.klage.search.exceptions..")
        .layer("Gateway").definedBy("no.nav.klage.search.gateway")

    @ArchTest
    val layer_dependencies_are_respected_for_controllers: ArchRule = kabalApiLayeredArchitecture()
        .whereLayer("Controllers").mayOnlyBeAccessedByLayers("Config")

    @ArchTest
    val layer_dependencies_are_respected_for_apimappers: ArchRule = kabalApiLayeredArchitecture()
        .whereLayer("ApiMappers").mayOnlyBeAccessedByLayers("Controllers", "Config")

    @ArchTest
    val layer_dependencies_are_respected_for_view: ArchRule = kabalApiLayeredArchitecture()
        .whereLayer("View").mayOnlyBeAccessedByLayers("Controllers", "Services", "Config", "ApiMappers")

    @ArchTest
    val layer_dependencies_are_respected_for_services: ArchRule = kabalApiLayeredArchitecture()
        .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers", "Config", "ApiMappers", "Clients")

    @ArchTest
    val layer_dependencies_are_respected_for_persistence: ArchRule = kabalApiLayeredArchitecture()
        .whereLayer("Repositories")
        .mayOnlyBeAccessedByLayers("Services", "Controllers", "Config", "ApiMappers")

    @ArchTest
    val layer_dependencies_are_respected_for_clients: ArchRule = kabalApiLayeredArchitecture()
        .whereLayer("Clients")
        .mayOnlyBeAccessedByLayers("Services", "Repositories", "Config", "Controllers", "Util", "ApiMappers", "Gateway")

}