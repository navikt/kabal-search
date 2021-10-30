package no.nav.klage.search.repositories

import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface EsKlagebehandlingRepository : ElasticsearchRepository<EsKlagebehandling, String>