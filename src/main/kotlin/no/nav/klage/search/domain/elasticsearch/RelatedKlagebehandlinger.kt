package no.nav.klage.search.domain.elasticsearch

data class RelatedKlagebehandlinger(
    val aapneByFnr: List<EsKlagebehandling>,
    val avsluttedeByFnr: List<EsKlagebehandling>,
    val aapneBySaksreferanse: List<EsKlagebehandling>,
    val avsluttedeBySaksreferanse: List<EsKlagebehandling>,
)
