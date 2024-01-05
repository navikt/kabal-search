package no.nav.klage.search.service.unleash
/*
import no.finn.unleash.strategy.Strategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ByClusterStrategy(@Value("\${nais.cluster.name}") val currentCluster: String) : Strategy {

    companion object {
        const val PARAM = "cluster"
    }

    override fun getName(): String = "byCluster"

    override fun isEnabled(parameters: MutableMap<String, String>): Boolean =
        getEnabledClusters(parameters)?.any { isCurrentClusterEnabled(it) } ?: false

    private fun getEnabledClusters(parameters: MutableMap<String, String>) =
        parameters[PARAM]?.split(',')

    private fun isCurrentClusterEnabled(cluster: String): Boolean {
        return currentCluster == cluster
    }
}

 */