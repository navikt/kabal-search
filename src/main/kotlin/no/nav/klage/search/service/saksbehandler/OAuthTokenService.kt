package no.nav.klage.search.service.saksbehandler

import no.nav.klage.search.util.TokenUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class OAuthTokenService(
    private val tokenUtil: TokenUtil,
    @Value("\${KABAL_OPPGAVESTYRING_EGEN_ENHET}") private val kabalOppgavestyringEgenEnhet: String,
    @Value("\${FORTROLIG}") private val fortrolig: String,
    @Value("\${STRENGT_FORTROLIG}") private val strengtFortrolig: String,
    @Value("\${EGEN_ANSATT}") private val egenAnsatt: String,
    @Value("\${KABAL_ADMIN}") private val kabalAdmin: String
) {

    fun getInnloggetIdent() = tokenUtil.getIdent()

    fun isAdmin(): Boolean = tokenUtil.getRollerFromToken().hasRole(kabalAdmin)

    fun isKabalOppgavestyringEgenEnhet(): Boolean = tokenUtil.getRollerFromToken().hasRole(kabalOppgavestyringEgenEnhet)

    fun kanBehandleFortrolig(): Boolean = tokenUtil.getRollerFromToken().hasRole(fortrolig)

    fun kanBehandleStrengtFortrolig(): Boolean =
        tokenUtil.getRollerFromToken().hasRole(strengtFortrolig)

    fun kanBehandleEgenAnsatt(): Boolean = tokenUtil.getRollerFromToken().hasRole(egenAnsatt)

    private fun List<String>.hasRole(role: String) = any { it.contains(role) }
}
