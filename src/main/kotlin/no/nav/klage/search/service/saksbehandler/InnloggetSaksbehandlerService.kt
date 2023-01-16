package no.nav.klage.search.service.saksbehandler

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.gateway.AzureGateway
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerService(
    private val azureGateway: AzureGateway,
    private val tokenUtil: TokenUtil,
    private val saksbehandlerService: SaksbehandlerService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getEnhetForSaksbehandler(): Enhet {
        return azureGateway.getDataOmInnloggetSaksbehandler().enhet
    }

    fun getTildelteYtelserForSaksbehandler(): List<Ytelse> {
        val innloggetSaksbehandlerIdent = tokenUtil.getIdent()
        return saksbehandlerService.getTildelteYtelserForSaksbehandler(innloggetSaksbehandlerIdent)
    }
}
