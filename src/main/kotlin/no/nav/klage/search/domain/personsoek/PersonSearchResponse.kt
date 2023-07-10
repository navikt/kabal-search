package no.nav.klage.search.domain.personsoek

import no.nav.klage.search.domain.elasticsearch.EsAnonymBehandling

data class PersonSearchResponse(
    val behandlinger: List<EsAnonymBehandling>
)