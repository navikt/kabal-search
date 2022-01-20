package no.nav.klage.search.service.saksbehandler

import no.nav.klage.search.util.TokenUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class OAuthTokenService(
    private val tokenUtil: TokenUtil,
    @Value("\${ROLE_KLAGE_SAKSBEHANDLER}") private val saksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_FAGANSVARLIG}") private val fagansvarligRole: String,
    @Value("\${ROLE_KLAGE_LEDER}") private val lederRole: String,
    @Value("\${ROLE_KLAGE_MERKANTIL}") private val merkantilRole: String,
    @Value("\${ROLE_KLAGE_FORTROLIG}") private val kanBehandleFortroligRole: String,
    @Value("\${ROLE_KLAGE_STRENGT_FORTROLIG}") private val kanBehandleStrengtFortroligRole: String,
    @Value("\${ROLE_KLAGE_EGEN_ANSATT}") private val kanBehandleEgenAnsattRole: String,
    @Value("\${ROLE_ADMIN}") private val adminRole: String
) {

    fun getInnloggetIdent() = tokenUtil.getIdent()

    fun isAdmin(): Boolean = tokenUtil.getRollerFromToken().hasRole(adminRole)

    fun isLeder(): Boolean = tokenUtil.getRollerFromToken().hasRole(lederRole)

    fun isFagansvarlig(): Boolean = tokenUtil.getRollerFromToken().hasRole(fagansvarligRole)

    fun isSaksbehandler(): Boolean = tokenUtil.getRollerFromToken().hasRole(saksbehandlerRole)

    fun kanBehandleFortrolig(): Boolean = tokenUtil.getRollerFromToken().hasRole(kanBehandleFortroligRole)

    fun kanBehandleStrengtFortrolig(): Boolean =
        tokenUtil.getRollerFromToken().hasRole(kanBehandleStrengtFortroligRole)

    fun kanBehandleEgenAnsatt(): Boolean = tokenUtil.getRollerFromToken().hasRole(kanBehandleEgenAnsattRole)

    private fun List<String>.hasRole(role: String) = any { it.contains(role) }
}
