package no.nav.klage.search.config

import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.influx.InfluxMeterRegistry
import no.nav.klage.search.util.getLogger
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MetricsConfiguration {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Bean
    fun influxMetricsCustomization(): MeterRegistryCustomizer<InfluxMeterRegistry>? {
        return MeterRegistryCustomizer<InfluxMeterRegistry> { registry ->
            registry.config().meterFilter(
                MeterFilter.denyUnless { it.name.startsWith("funksjonell") }
            )
        }
    }
}