package no.nav.klage.search.service

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import java.time.Duration


class TestOpenSearchContainer private constructor() :
    GenericContainer<TestOpenSearchContainer>(IMAGE_VERSION) {

    companion object {
        private const val IMAGE_VERSION = "opensearchproject/opensearch:3"

        private val CONTAINER: TestOpenSearchContainer = TestOpenSearchContainer()

        val instance: TestOpenSearchContainer
            get() {
                return CONTAINER
            }
    }

    override fun start() {
        withNetwork(network)
        addExposedPort(9200)
        setWaitStrategy(
            HttpWaitStrategy()
                .forPort(9200)
                .forStatusCodeMatching { response: Int -> response == 200 || response == 401 }
                .withStartupTimeout(Duration.ofMinutes(5)))
        withEnv("discovery.type", "single-node")
        withEnv("DISABLE_INSTALL_DEMO_CONFIG", "true")
        withEnv("DISABLE_SECURITY_PLUGIN", "true")

        super.start()
        System.setProperty("OPEN_SEARCH_USERNAME", "whatever")
        System.setProperty("OPEN_SEARCH_PASSWORD", "changeme")
        System.setProperty(
            "OPEN_SEARCH_URI", "http://${CONTAINER.host}:${CONTAINER.firstMappedPort}"
        )
    }

    override fun stop() {
        //CONTAINER.stop()
        //do nothing, JVM handles shut down
    }


}