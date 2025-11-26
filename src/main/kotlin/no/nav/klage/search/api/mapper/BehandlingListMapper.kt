package no.nav.klage.search.api.mapper


import no.nav.klage.search.api.view.FnrSearchResponseWithoutPerson
import no.nav.klage.search.domain.elasticsearch.EsAnonymBehandling
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import org.springframework.stereotype.Component

@Component
class BehandlingListMapper {

    fun mapPersonSearchResponseToFnrSearchResponseWithoutPerson(
        personSearchResponse: PersonSearchResponse,
    ): FnrSearchResponseWithoutPerson {
        val behandlinger = personSearchResponse.behandlinger

        return FnrSearchResponseWithoutPerson(
            aapneBehandlinger = mapEsBehandlingerToListView(behandlinger.filter { it.feilregistrert == null && it.avsluttetAvSaksbehandler == null && it.sattPaaVent == null }),
            avsluttedeBehandlinger = mapEsBehandlingerToListView(behandlinger.filter { it.feilregistrert == null && it.avsluttetAvSaksbehandler != null }
                .sortedByDescending { it.avsluttetAvSaksbehandler }),
            feilregistrerteBehandlinger = mapEsBehandlingerToListView(behandlinger.filter { it.feilregistrert != null }
                .sortedByDescending { it.feilregistrert }),
            paaVentBehandlinger = mapEsBehandlingerToListView(behandlinger.filter { it.feilregistrert == null && it.avsluttetAvSaksbehandler == null && it.sattPaaVent != null }),
        )
    }

    fun mapEsBehandlingerToListView(
        esBehandlinger: List<EsAnonymBehandling>,
    ): List<String> {
        return esBehandlinger.map { it.behandlingId }
    }
}