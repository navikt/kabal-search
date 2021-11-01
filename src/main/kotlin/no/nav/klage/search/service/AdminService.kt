package no.nav.klage.search.service

import no.nav.klage.search.clients.klageendret.KlageEndretKafkaConsumer
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val indexService: IndexService,
    private val klageEndretKafkaConsumer: KlageEndretKafkaConsumer
) {

    fun recreateEsIndex() {
        indexService.recreateIndex()
    }

    fun syncEsWithKafka() {
        klageEndretKafkaConsumer.consumeFromBeginning()
    }

    fun deleteAllInES() {
        indexService.deleteAllKlagebehandlinger()
    }
}