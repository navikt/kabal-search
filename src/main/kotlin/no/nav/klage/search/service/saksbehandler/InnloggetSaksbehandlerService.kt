package no.nav.klage.search.service.saksbehandler

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.klageenhetTilYtelser
import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeYtelser
import no.nav.klage.search.gateway.AzureGateway
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerService(
    private val azureGateway: AzureGateway,
    private val oAuthTokenService: OAuthTokenService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getEnheterMedYtelserForSaksbehandler(): EnheterMedLovligeYtelser {
        if (oAuthTokenService.iskabalOppgavestyringAlleEnheter()) {
            return EnheterMedLovligeYtelser(
                klageenhetTilYtelser.map {
                    val enhet = Enhet(it.key.navn, it.key.navn)
                    EnhetMedLovligeYtelser(
                        enhet = enhet,
                        ytelser = getYtelserForEnhet(enhet)
                    )
                }
            )
        }
        return listOf(azureGateway.getDataOmInnloggetSaksbehandler().enhet).berikMedYtelser()
    }

    fun getEnhetMedYtelserForSaksbehandler(): EnhetMedLovligeYtelser =
        azureGateway.getDataOmInnloggetSaksbehandler().enhet.berikMedYtelser()

    private fun List<Enhet>.berikMedYtelser(): EnheterMedLovligeYtelser {
        return EnheterMedLovligeYtelser(this.map { it.berikMedYtelser() })
    }

    private fun Enhet.berikMedYtelser() = EnhetMedLovligeYtelser(
        enhet = this,
        ytelser = getYtelserForEnhet(this)
    )

    private fun getYtelserForEnhet(enhet: Enhet): List<Ytelse> =
        klageenhetTilYtelser.filter { it.key.navn == enhet.enhetId }.flatMap { it.value }

}
