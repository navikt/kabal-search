package no.nav.klage.search.service

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.search.config.CacheWithJCacheConfiguration.Companion.METRICS_CACHE
import no.nav.klage.search.config.FunksjonelleGaugesConfiguration.Companion.funksjonellAntallSaksdokumenterPaaAvsluttedeBehandlingerMedian
import no.nav.klage.search.config.FunksjonelleGaugesConfiguration.Companion.funksjonellAvsluttet
import no.nav.klage.search.config.FunksjonelleGaugesConfiguration.Companion.funksjonellIkkeTildelt
import no.nav.klage.search.config.FunksjonelleGaugesConfiguration.Companion.funksjonellMedunderskriverValgt
import no.nav.klage.search.config.FunksjonelleGaugesConfiguration.Companion.funksjonellPaaVent
import no.nav.klage.search.config.FunksjonelleGaugesConfiguration.Companion.funksjonellReturnertTilSaksbehandler
import no.nav.klage.search.config.FunksjonelleGaugesConfiguration.Companion.funksjonellSendtTilMedunderskriver
import no.nav.klage.search.config.FunksjonelleGaugesConfiguration.Companion.funksjonellTildelt
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CachedMetricsService(
    private val elasticsearchService: ElasticsearchService,
) {

    @Cacheable(METRICS_CACHE)
    fun getCachedMetrics(): Map<String, Number> {
        val metricsMap = mutableMapOf<String, Number>()

        Ytelse.entries.forEach { ytelse ->
            Type.entries.forEach { type ->
                val suffix = "+$ytelse-$type"
                metricsMap["$funksjonellIkkeTildelt$suffix"] =
                    elasticsearchService.countIkkeTildelt(ytelse, type)
                metricsMap["$funksjonellTildelt$suffix"] =
                    elasticsearchService.countTildelt(ytelse, type)
                metricsMap["$funksjonellMedunderskriverValgt$suffix"] =
                    elasticsearchService.countMedunderskriverValgt(ytelse, type)
                metricsMap["$funksjonellSendtTilMedunderskriver$suffix"] =
                    elasticsearchService.countSendtTilMedunderskriver(ytelse, type)
                metricsMap["$funksjonellReturnertTilSaksbehandler$suffix"] =
                    elasticsearchService.countReturnertTilSaksbehandler(ytelse, type)
                metricsMap["$funksjonellAvsluttet$suffix"] =
                    elasticsearchService.countAvsluttet(ytelse, type)
                metricsMap["$funksjonellAntallSaksdokumenterPaaAvsluttedeBehandlingerMedian$suffix"] =
                    elasticsearchService.countAntallSaksdokumenterIAvsluttedeBehandlingerMedian(ytelse, type)
                metricsMap["$funksjonellPaaVent$suffix"] = elasticsearchService.countSattPaaVent(ytelse, type)
            }
        }
        return metricsMap
    }
}