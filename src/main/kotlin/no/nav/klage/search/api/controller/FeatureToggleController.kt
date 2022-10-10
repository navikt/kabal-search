package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Hidden
import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.klage.search.config.SecurityConfiguration
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Hidden
class FeatureToggleController(
    private val unleash: Unleash,
    private val oAuthTokenService: OAuthTokenService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
    @GetMapping("/featuretoggle/{toggleName}")
    fun getToggle(@PathVariable("toggleName") toggleName: String): Boolean =
        isEnabled(toggleName)

    @Unprotected
    @GetMapping("/aapenfeaturetoggle/{toggleName}")
    fun getUnprotectedToggle(@PathVariable("toggleName") toggleName: String): Boolean =
        unleash.isEnabled(toggleName, UnleashContext.builder().userId("UINNLOGGET").build())

    private fun isEnabled(feature: String): Boolean =
        unleash.isEnabled(feature, contextMedInnloggetBruker())

    private fun contextMedInnloggetBruker(): UnleashContext =
        UnleashContext.builder().userId(getIdent()).build()

    private fun getIdent() = try {
        oAuthTokenService.getInnloggetIdent()
    } catch (e: Exception) {
        logger.info("Not able to retrieve token", e)
        "UINNLOGGET"
    }


}
