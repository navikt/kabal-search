package no.nav.klage.search.domain.elasticsearch

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class EsVedtak(

    val utfall: String?,
    val grunn: String?,
    val hjemler: List<String>,
    val brevmottakerFnr: List<String>,
    val brevmottakerOrgnr: List<String>,
    val journalpostId: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val created: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val modified: LocalDateTime,
)
