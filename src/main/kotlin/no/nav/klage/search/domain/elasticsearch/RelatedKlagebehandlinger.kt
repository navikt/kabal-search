package no.nav.klage.search.domain.elasticsearch

data class RelatedKlagebehandlinger(
    val aapneByFnr: List<EsBehandling>,
    val avsluttedeByFnr: List<EsBehandling>,
    val aapneBySaksreferanse: List<EsBehandling>,
    val avsluttedeBySaksreferanse: List<EsBehandling>,
)
