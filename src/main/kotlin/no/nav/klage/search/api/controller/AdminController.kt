package no.nav.klage.search.api.controller

import no.nav.klage.search.config.SecurityConfiguration
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.service.AdminService
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
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
    private val oAuthTokenService: OAuthTokenService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/internal/elasticadmin/recreate", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun recreateElasticIndexWithPost() {
        validateUserIsAdmin()
        try {
            adminService.recreateEsIndex()
        } catch (e: Exception) {
            logger.warn("Failed to recreate ES index", e)
            throw e
        }
    }

    private fun validateUserIsAdmin() {
        if (!oAuthTokenService.isAdmin()) {
            throw MissingTilgangException("Not an admin")
        }
    }
}