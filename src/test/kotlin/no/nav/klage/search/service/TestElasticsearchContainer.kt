package no.nav.klage.search.service

import org.testcontainers.elasticsearch.ElasticsearchContainer

class TestElasticsearchContainer private constructor() :
    ElasticsearchContainer(IMAGE_VERSION) {

    companion object {
        private const val IMAGE_VERSION = "docker.elastic.co/elasticsearch/elasticsearch:7.10.2"

        private val CONTAINER: TestElasticsearchContainer = TestElasticsearchContainer()

        val instance: TestElasticsearchContainer
            get() {
                return CONTAINER
            }
    }

    override fun start() {
        super.start()
        System.setProperty("ELASTIC_USERNAME", "elastic")
        System.setProperty("ELASTIC_PASSWORD", "changeme")
        System.setProperty("ELASTIC_URI", "http://${CONTAINER.host}:${CONTAINER.firstMappedPort}")
    }

    override fun stop() {
        //do nothing, JVM handles shut down
    }


}