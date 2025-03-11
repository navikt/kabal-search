package no.nav.klage.search.config

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PreDestroy


@Configuration
class FunksjonelleGaugesConfiguration {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Bean
    fun registerFunctionalStats(elasticsearchService: ElasticsearchService): MeterBinder {
        return try {
            MeterBinder { registry: MeterRegistry ->
                Ytelse.entries.forEach { ytelse ->
                    Type.entries.forEach { type ->
                        Gauge.builder("funksjonell.ikketildelt") { elasticsearchService.countIkkeTildelt(ytelse, type) }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder("funksjonell.tildelt") { elasticsearchService.countTildelt(ytelse, type) }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder("funksjonell.medunderskrivervalgt") { elasticsearchService.countMedunderskriverValgt(ytelse, type) }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder("funksjonell.sendttilmedunderskriver") { elasticsearchService.countSendtTilMedunderskriver(ytelse, type) }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder("funksjonell.returnerttilsaksbehandler") { elasticsearchService.countReturnertTilSaksbehandler(ytelse, type) }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder("funksjonell.avsluttet") { elasticsearchService.countAvsluttet(ytelse, type) }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder("funksjonell.antallsaksdokumenterpaaavsluttedebehandlinger.median") { elasticsearchService.countAntallSaksdokumenterIAvsluttedeBehandlingerMedian(ytelse, type) }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)

                        Gauge.builder("funksjonell.paavent") { elasticsearchService.countSattPaaVent(ytelse, type) }
                            .tag("ytelse", ytelse.navn)
                            .tag("type", type.navn)
                            .register(registry)
                    }
                }
            }
        } catch (e: Exception) {
            secureLogger.error("Could not setup gauges", e)
            throw RuntimeException("Could not setup gauges. See details in secure logs.")
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