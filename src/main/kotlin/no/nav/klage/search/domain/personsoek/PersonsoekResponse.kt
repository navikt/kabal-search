package no.nav.klage.search.domain.personsoek

import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import java.time.LocalDate

data class PersonSoekResponse(
    val fnr: String,
    val navn: String?,
    val foedselsdato: LocalDate?,
    val klagebehandlinger: List<EsKlagebehandling>
)
