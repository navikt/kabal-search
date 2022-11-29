package no.nav.klage.search.service.saksbehandler

import no.nav.klage.search.util.TokenUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class OAuthTokenService(
    private val tokenUtil: TokenUtil,
    @Value("\${KABAL_OPPGAVESTYRING_ALLE_ENHETER_ROLE_ID}") private val kabalOppgavestyringAlleEnheterRoleId: String,
    @Value("\${KABAL_INNSYN_EGEN_ENHET_ROLE_ID}") private val kabalOppgavestyringEgenEnhetRoleId: String,
    @Value("\${FORTROLIG_ROLE_ID}") private val fortroligRoleId: String,
    @Value("\${STRENGT_FORTROLIG_ROLE_ID}") private val strengtFortroligRoleId: String,
    @Value("\${EGEN_ANSATT_ROLE_ID}") private val egenAnsattRoleId: String,
    @Value("\${KABAL_ADMIN_ROLE_ID}") private val kabalAdminRoleId: String
) {

    fun getInnloggetIdent() = tokenUtil.getIdent()

    fun isAdmin(): Boolean = tokenUtil.getRoleIdsFromToken().contains(kabalAdminRoleId)

    fun isKabalOppgavestyringEgenEnhet(): Boolean = tokenUtil.getRoleIdsFromToken().contains(kabalOppgavestyringEgenEnhetRoleId)

    fun iskabalOppgavestyringAlleEnheter(): Boolean = tokenUtil.getRoleIdsFromToken().contains(kabalOppgavestyringAlleEnheterRoleId)

    fun kanBehandleFortrolig(): Boolean = tokenUtil.getRoleIdsFromToken().contains(fortroligRoleId)

    fun kanBehandleStrengtFortrolig(): Boolean =
        tokenUtil.getRoleIdsFromToken().contains(strengtFortroligRoleId)

    fun kanBehandleEgenAnsatt(): Boolean = tokenUtil.getRoleIdsFromToken().contains(egenAnsattRoleId)
}
