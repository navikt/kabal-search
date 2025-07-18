package no.nav.klage.search.service

import no.nav.klage.search.domain.OppgaverOmPersonSearchCriteria
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service

@Service
class PersonSearchService(
    private val elasticsearchService: ElasticsearchService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun fnrSearch(input: OppgaverOmPersonSearchCriteria): PersonSearchResponse? {
        val searchHitsInES = esSoek(input)
        logger.debug("fnrSearch: Got ${searchHitsInES.size} hits from ES")
        return if (searchHitsInES.isEmpty()) {
            null
        } else {
            PersonSearchResponse(
                behandlinger = searchHitsInES
            )
        }
    }

    private fun esSoek(input: OppgaverOmPersonSearchCriteria): List<EsBehandling> {
        val esResponse = elasticsearchService.findOppgaverOmPersonByCriteria(input)
        return esResponse.searchHits.map { it.content }
    }
}
