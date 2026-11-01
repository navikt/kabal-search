package no.nav.klage.search.config

import no.nav.klage.auth.test.AzureSecurityContractTest
import no.nav.klage.search.api.controller.FeatureToggleController
import org.junit.jupiter.api.Test
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(FeatureToggleController::class)
@Import(SecurityConfiguration::class)
class SecurityConfigurationTest : AzureSecurityContractTest() {
    override fun protectedRequest() = get("/featuretoggle/toggle")

    override fun unauthenticatedPaths() = listOf("/internal/health", "/aapenfeaturetoggle/toggle")

    @Test
    fun `dev-only admin paths require token outside dev`() {
        mockMvc.perform(get("/internal/elasticadmin/nuke")).andExpect(status().isUnauthorized)
        mockMvc.perform(delete("/internal/behandlinger/${UUID.randomUUID()}")).andExpect(status().isUnauthorized)
    }
}
