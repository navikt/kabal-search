package no.nav.klage.search.config

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import jakarta.annotation.PreDestroy
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.search.service.CachedMetricsService
import no.nav.klage.search.util.getLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class FunksjonelleGaugesConfiguration {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        const val funksjonellIkkeTildelt = "funksjonell.ikketildelt"
        const val funksjonellTildelt = "funksjonell.tildelt"
        const val funksjonellMedunderskriverValgt = "funksjonell.medunderskrivervalgt"
        const val funksjonellSendtTilMedunderskriver = "funksjonell.sendttilmedunderskriver"
        const val funksjonellReturnertTilSaksbehandler = "funksjonell.returnerttilsaksbehandler"
        const val funksjonellAvsluttet = "funksjonell.avsluttet"
        const val funksjonellAntallSaksdokumenterPaaAvsluttedeBehandlingerMedian =
            "funksjonell.antallsaksdokumenterpaaavsluttedebehandlinger.median"
        const val funksjonellPaaVent = "funksjonell.paavent"
    }

    @Bean
    fun registerFunctionalStats(cachedMetricsService: CachedMetricsService): MeterBinder {
        return try {
            MeterBinder { registry: MeterRegistry ->
                Ytelse.entries.forEach { ytelse ->
                    Type.entries.forEach { type ->
                        val suffix = "+$ytelse-$type"
                        Gauge.builder(funksjonellIkkeTildelt) { cachedMetricsService.getCachedMetrics()["$funksjonellIkkeTildelt$suffix"] }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder(funksjonellTildelt) { cachedMetricsService.getCachedMetrics()["$funksjonellTildelt$suffix"] }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder(funksjonellMedunderskriverValgt) { cachedMetricsService.getCachedMetrics()["$funksjonellMedunderskriverValgt$suffix"] }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder(funksjonellSendtTilMedunderskriver) { cachedMetricsService.getCachedMetrics()["$funksjonellSendtTilMedunderskriver$suffix"] }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder(funksjonellReturnertTilSaksbehandler) { cachedMetricsService.getCachedMetrics()["$funksjonellReturnertTilSaksbehandler$suffix"] }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder(funksjonellAvsluttet) { cachedMetricsService.getCachedMetrics()["$funksjonellAvsluttet$suffix"] }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder(funksjonellAntallSaksdokumenterPaaAvsluttedeBehandlingerMedian) { cachedMetricsService.getCachedMetrics()["$funksjonellAntallSaksdokumenterPaaAvsluttedeBehandlingerMedian$suffix"] }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder(funksjonellPaaVent) { cachedMetricsService.getCachedMetrics()["$funksjonellPaaVent$suffix"] }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Could not setup gauges.", e)
            throw RuntimeException("Could not setup gauges.", e)
        }
    }
}

@Configuration
class MicrometerGracefulShutdownConfiguration {

    @Bean
    fun registryCloser(registry: MeterRegistry) = RegistryCloser(registry)
}

class RegistryCloser(private val registry: MeterRegistry) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PreDestroy
    fun onDestroy() {
        logger.info("We have received a SIGTERM (?)")
        if (!registry.isClosed) registry.close()
    }
}