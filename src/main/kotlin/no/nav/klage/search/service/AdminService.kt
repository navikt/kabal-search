package no.nav.klage.search.service

import no.nav.klage.search.clients.klageendret.BehandlingEndretKafkaConsumer
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val indexService: IndexService,
    private val behandlingEndretKafkaConsumer: BehandlingEndretKafkaConsumer
) {

    fun recreateEsIndex() {
        indexService.recreateIndex()
    }

    fun syncEsWithKafka() {
        behandlingEndretKafkaConsumer.consumeFromBeginning()
    }

    fun deleteAllInES() {
        indexService.deleteAllBehandlinger()
    }
}