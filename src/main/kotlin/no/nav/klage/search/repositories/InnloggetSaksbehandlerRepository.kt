package no.nav.klage.search.repositories

import no.nav.klage.search.domain.kodeverk.Tema
import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeTemaer
import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeYtelser
import no.nav.klage.search.util.TokenUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerRepository(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val tokenUtil: TokenUtil,
    @Value("\${ROLE_GOSYS_OPPGAVE_BEHANDLER}") private val gosysSaksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_SAKSBEHANDLER}") private val saksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_FAGANSVARLIG}") private val fagansvarligRole: String,
    @Value("\${ROLE_KLAGE_LEDER}") private val lederRole: String,
    @Value("\${ROLE_KLAGE_MERKANTIL}") private val merkantilRole: String,
    @Value("\${ROLE_KLAGE_FORTROLIG}") private val kanBehandleFortroligRole: String,
    @Value("\${ROLE_KLAGE_STRENGT_FORTROLIG}") private val kanBehandleStrengtFortroligRole: String,
    @Value("\${ROLE_KLAGE_EGEN_ANSATT}") private val kanBehandleEgenAnsattRole: String,
    @Value("\${ROLE_ADMIN}") private val adminRole: String
) {

    fun getEnheterMedTemaerForSaksbehandler(): EnheterMedLovligeTemaer =
        saksbehandlerRepository.getEnheterMedTemaerForSaksbehandler(getInnloggetIdent())

    fun getEnheterMedYtelserForSaksbehandler(): EnheterMedLovligeYtelser =
        saksbehandlerRepository.getEnheterMedYtelserForSaksbehandler(getInnloggetIdent())

    fun getInnloggetIdent() = tokenUtil.getIdent()

    fun isAdmin(): Boolean = tokenUtil.getRollerFromToken().hasRole(adminRole)

    fun isLeder(): Boolean = tokenUtil.getRollerFromToken().hasRole(lederRole)

    fun isFagansvarlig(): Boolean = tokenUtil.getRollerFromToken().hasRole(fagansvarligRole)

    fun isSaksbehandler(): Boolean =
        tokenUtil.getRollerFromToken().hasRole(saksbehandlerRole) || tokenUtil.getRollerFromToken()
            .hasRole(gosysSaksbehandlerRole)

    fun kanBehandleFortrolig(): Boolean = tokenUtil.getRollerFromToken().hasRole(kanBehandleFortroligRole)

    fun kanBehandleStrengtFortrolig(): Boolean =
        tokenUtil.getRollerFromToken().hasRole(kanBehandleStrengtFortroligRole)

    fun kanBehandleEgenAnsatt(): Boolean = tokenUtil.getRollerFromToken().hasRole(kanBehandleEgenAnsattRole)

    fun harTilgangTilEnhet(enhetId: String): Boolean {
        return saksbehandlerRepository.harTilgangTilEnhet(getInnloggetIdent(), enhetId)
    }

    fun harTilgangTilTema(tema: Tema): Boolean {
        return saksbehandlerRepository.harTilgangTilTema(getInnloggetIdent(), tema)
    }

    fun harTilgangTilEnhetOgTema(enhetId: String, tema: Tema): Boolean {
        return saksbehandlerRepository.harTilgangTilEnhetOgTema(getInnloggetIdent(), enhetId, tema)
    }

    private fun List<String>.hasRole(role: String) = any { it.contains(role) }
}
