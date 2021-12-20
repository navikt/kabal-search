package no.nav.klage.search.service.elasticsearch

import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling


class EsKlagebehandlingRepository(
    private val createIndexService: CreateIndexService,
    private val saveService: SaveService,
) {
    fun save(klagebehandling: EsKlagebehandling) {
        saveService.save(klagebehandling)
    }

    fun saveAll(klagebehandlinger: List<EsKlagebehandling>) {
        saveService.save(klagebehandlinger)
    }
    
}