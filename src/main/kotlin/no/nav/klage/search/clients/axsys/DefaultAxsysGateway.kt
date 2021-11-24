package no.nav.klage.search.clients.axsys

import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.gateway.AxsysGateway
import org.springframework.stereotype.Service

@Service
class DefaultAxsysGateway(
    private val axsysClient: AxsysClient,
    private val tilgangerMapper: TilgangerMapper
) : AxsysGateway {

    override fun getEnheterForSaksbehandler(ident: String): List<Enhet> =
        tilgangerMapper.mapTilgangerToEnheter(axsysClient.getTilgangerForSaksbehandler(ident))
}