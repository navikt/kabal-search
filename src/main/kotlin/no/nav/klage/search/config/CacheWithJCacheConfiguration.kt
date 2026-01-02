package no.nav.klage.search.config


import no.nav.klage.search.util.getLogger
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.util.concurrent.TimeUnit
import javax.cache.CacheManager
import javax.cache.configuration.MutableConfiguration
import javax.cache.expiry.CreatedExpiryPolicy
import javax.cache.expiry.Duration


@EnableCaching
@Configuration
class CacheWithJCacheConfiguration(private val environment: Environment) : JCacheManagerCustomizer {

    companion object {

        const val AZUREUSER_CACHE = "azureuser"
        const val METRICS_CACHE = "metrics"

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    override fun customize(cacheManager: CacheManager) {
        cacheManager.createCache(AZUREUSER_CACHE, cacheConfiguration())
        cacheManager.createCache(METRICS_CACHE, cacheConfiguration(10L))
    }

    private fun cacheConfiguration(duration: Long? = null) =
        MutableConfiguration<Any, Any>()
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration(TimeUnit.MINUTES, duration ?: commonDuration())))
            .setStoreByValue(false)
            .setStatisticsEnabled(true)

    private fun commonDuration() =
        if (environment.activeProfiles.contains("prod")) {
            480L
        } else {
            10L
        }

}