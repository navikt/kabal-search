package no.nav.klage.search.domain.personsoek

import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling

data class PersonSearchResponse(
    val fnr: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val klagebehandlinger: List<EsKlagebehandling>
)