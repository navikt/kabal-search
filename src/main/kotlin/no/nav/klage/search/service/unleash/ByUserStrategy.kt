package no.nav.klage.search.service.unleash

import no.finn.unleash.strategy.Strategy
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import org.springframework.stereotype.Component

@Component
class ByUserStrategy(private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository) : Strategy {

    companion object {
        const val PARAM = "user"
    }

    override fun getName(): String = "byUserId"

    override fun isEnabled(parameters: Map<String, String>?): Boolean =
        getEnabledUsers(parameters)?.any { isCurrentUserEnabled(it) } ?: false

    private fun getEnabledUsers(parameters: Map<String, String>?) =
        parameters?.get(PARAM)?.split(',')

    private fun isCurrentUserEnabled(ident: String): Boolean {
        return ident == innloggetSaksbehandlerRepository.getInnloggetIdent()
    }

}