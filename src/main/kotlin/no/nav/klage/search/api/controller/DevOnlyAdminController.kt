package no.nav.klage.search.api.controller

import no.nav.klage.search.service.AdminService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Profile("dev-gcp")
@RestController
class DevOnlyAdminController(
    private val adminService: AdminService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO remember to change URL in e2e-tests
    @Unprotected
    @GetMapping("/internal/elasticadmin/nuke", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun recreateElasticIndex() {
        try {
            adminService.recreateEsIndex()
        } catch (e: Exception) {
            logger.warn("Failed to reset ES index", e)
            throw e
        }
    }

    @Unprotected
    @DeleteMapping("/internal/behandlinger/{id}", produces = ["application/json"])
    fun deleteBehandlingFromElasticIndex(
        @PathVariable("id") behandlingId: UUID,
    ) {
        try {
            adminService.deleteBehandling(behandlingId)
        } catch (e: Exception) {
            logger.warn("Failed to delete behandling with id $behandlingId", e)
            throw e
        }
    }
}