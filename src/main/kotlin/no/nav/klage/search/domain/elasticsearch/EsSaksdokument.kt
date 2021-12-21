package no.nav.klage.search.domain.elasticsearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EsSaksdokument(
    val journalpostId: String,
    val dokumentInfoId: String
)

