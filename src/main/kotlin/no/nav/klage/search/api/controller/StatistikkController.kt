package no.nav.klage.search.api.controller

import no.nav.klage.search.domain.elasticsearch.KlageStatistikk
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatistikkController(private val elasticsearchService: ElasticsearchService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Unprotected
    @GetMapping("/statistikk/klagebehandlinger", produces = ["application/json"])
    fun getKlageStatistikk(): KlageStatistikk {
        logger.debug("getKlageStatistikk called")
        return elasticsearchService.statistikkQuery()
    }
}
