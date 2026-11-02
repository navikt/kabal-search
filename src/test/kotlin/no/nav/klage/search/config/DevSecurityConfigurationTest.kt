package no.nav.klage.search.config

import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import no.nav.klage.auth.test.AzureSecurityContractTest
import no.nav.klage.search.api.controller.DevOnlyAdminController
import no.nav.klage.search.service.AdminService
import org.junit.jupiter.api.Test
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(DevOnlyAdminController::class)
@Import(SecurityConfiguration::class)
@ActiveProfiles("dev")
class DevSecurityConfigurationTest : AzureSecurityContractTest() {
    @MockkBean(relaxed = true)
    lateinit var adminService: AdminService

    // Still protected in dev: only the DevOnlyAdminController paths are opened.
    override fun protectedRequest() = get("/internal/behandlinger/${UUID.randomUUID()}/delete")

    override fun assertAccepted(result: org.springframework.test.web.servlet.ResultActions) {
        // No AdminController in this slice, so a valid token gets 404.
        result.andExpect(status().isNotFound)
    }

    @Test
    fun `e2e admin endpoints are open in dev`() {
        val behandlingId = UUID.randomUUID()

        mockMvc.perform(get("/internal/elasticadmin/nuke")).andExpect(status().isOk)
        mockMvc.perform(delete("/internal/behandlinger/$behandlingId")).andExpect(status().isOk)

        verify { adminService.recreateEsIndex() }
        verify { adminService.deleteBehandling(behandlingId) }
    }
}
