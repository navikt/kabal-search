package no.nav.klage.search

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.Architectures.layeredArchitecture

@AnalyzeClasses(packages = ["no.nav.klage.search"], importOptions = [ImportOption.DoNotIncludeTests::class])
class LayeredArchitectureTest {
    fun kabalApiLayeredArchitecture() =
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controllers")
            .definedBy("no.nav.klage.search.api.controller")
            .layer("ApiMappers")
            .definedBy("no.nav.klage.search.api.mapper")
            .layer("View")
            .definedBy("no.nav.klage.search.api.view")
            .layer("Services")
            .definedBy("no.nav.klage.search.service..")
            .layer("Repositories")
            .definedBy("no.nav.klage.search.repositories..")
            .layer("Clients")
            .definedBy("no.nav.klage.search.clients..")
            .layer("Config")
            .definedBy("no.nav.klage.search.config..")
            .layer("Domain")
            .definedBy("no.nav.klage.search.domain..")
            .layer("Util")
            .definedBy("no.nav.klage.search.util..")
            .layer("Exceptions")
            .definedBy("no.nav.klage.search.exceptions..")
            .layer("Gateway")
            .definedBy("no.nav.klage.search.gateway")

    @ArchTest
    val layerDependenciesAreRespectedForControllers: ArchRule =
        kabalApiLayeredArchitecture()
            .whereLayer("Controllers")
            .mayOnlyBeAccessedByLayers("Config")

    @ArchTest
    val layerDependenciesAreRespectedForApimappers: ArchRule =
        kabalApiLayeredArchitecture()
            .whereLayer("ApiMappers")
            .mayOnlyBeAccessedByLayers("Controllers", "Config", "Services")

    @ArchTest
    val layerDependenciesAreRespectedForView: ArchRule =
        kabalApiLayeredArchitecture()
            .whereLayer("View")
            .mayOnlyBeAccessedByLayers("Controllers", "Services", "Config", "ApiMappers")

    @ArchTest
    val layerDependenciesAreRespectedForServices: ArchRule =
        kabalApiLayeredArchitecture()
            .whereLayer("Services")
            .mayOnlyBeAccessedByLayers("Controllers", "Config", "ApiMappers", "Clients")

    @ArchTest
    val layerDependenciesAreRespectedForPersistence: ArchRule =
        kabalApiLayeredArchitecture()
            .whereLayer("Repositories")
            .mayOnlyBeAccessedByLayers("Services", "Controllers", "Config", "ApiMappers")

    @ArchTest
    val layerDependenciesAreRespectedForClients: ArchRule =
        kabalApiLayeredArchitecture()
            .whereLayer("Clients")
            .mayOnlyBeAccessedByLayers("Services", "Repositories", "Config", "Controllers", "Util", "ApiMappers", "Gateway")
}
