package no.nav.klage.search.service.unleash

import no.finn.unleash.strategy.Strategy
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import org.springframework.stereotype.Component

@Component
class ByUserStrategy(private val oAuthTokenService: OAuthTokenService) : Strategy {

    companion object {
        const val PARAM = "user"
    }

    override fun getName(): String = "byUserId"

    override fun isEnabled(parameters: MutableMap<String, String>): Boolean =
        getEnabledUsers(parameters)?.any { isCurrentUserEnabled(it) } ?: false

    private fun getEnabledUsers(parameters: MutableMap<String, String>) =
        parameters.get(PARAM)?.split(',')

    private fun isCurrentUserEnabled(ident: String): Boolean {
        return ident == oAuthTokenService.getInnloggetIdent()
    }

}