package no.nav.klage.search.api.controller

import no.nav.klage.search.config.SecurityConfiguration
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.service.AdminService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class AdminController(
    private val adminService: AdminService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/internal/elasticadmin/rebuild", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun resetElasticIndexWithPost() {

        krevAdminTilgang()
        try {
            adminService.recreateEsIndex()
            //TODO: Reread from Kafka
            //adminService.syncEsWithDb()
            //adminService.findAndLogOutOfSyncKlagebehandlinger()
        } catch (e: Exception) {
            logger.warn("Failed to reset ES index", e)
            throw e
        }
    }

    private fun krevAdminTilgang() {
        if (!innloggetSaksbehandlerRepository.erAdmin()) {
            throw MissingTilgangException("Not an admin")
        }
    }
}