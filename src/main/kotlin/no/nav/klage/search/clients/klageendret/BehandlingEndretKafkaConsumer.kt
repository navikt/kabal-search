package no.nav.klage.search.clients.klageendret

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.search.service.IndexService
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AbstractConsumerSeekAware
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import java.util.*


@Component
class BehandlingEndretKafkaConsumer(
    private val indexService: IndexService,
) : AbstractConsumerSeekAware() {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

    //Dokumentasjonen er her:
    //https://docs.spring.io/spring-kafka/docs/2.5.5.RELEASE/reference/html/#seek
    fun consumeFromBeginning() {
        logger.info("Seeking to beginning of topic partitions")
        seekCallbacks.forEach { (tp, callback) ->
            logger.info("Seeking to beginning of topic ${tp.topic()} and partition ${tp.partition()}")
            callback.seekToBeginning(tp.topic(), tp.partition())
        }
        logger.info("Finished consumeFromBeginning")
    }

    @KafkaListener(
        id = "behandlingEndretListener",
        idIsGroup = false,
        topics = ["\${BEHANDLING_ENDRET_KAFKA_TOPIC_V2}"],
        containerFactory = "behandlingEndretKafkaListenerContainerFactory"
    )
    fun listenToBehandlingEndret(
        record: ConsumerRecord<String, String>,
        //ack: Acknowledgment,
        @Header(KafkaHeaders.GROUP_ID) groupId: String
    ) {
        runCatching {
            logger.debug("Reading offset ${record.offset()} from partition ${record.partition()} on kafka topic ${record.topic()} using groupId $groupId")
            val behandlingId = record.key()
            logger.debug("Read behandling with id $behandlingId")

            if (record.value() == null) {
                logger.debug("Behandling with id $behandlingId has null value. Means delete.")
                indexService.deleteBehandling(UUID.fromString(behandlingId))
            } else {
                val behandling = record.value().toBehandling()
                indexService.indexBehandling(behandling)
                logger.debug("Successfully indexed behandling with id $behandlingId")
                //logger.debug("Successfully indexed klagebehandling with id $klagebehandlingId, now acking record")
                //ack.acknowledge()
            }
        }.onFailure {
            secureLogger.error("Failed to process endret behandling record", it)
            throw RuntimeException("Could not process endret behandling record. See more details in secure log.")
        }
    }

    private fun String.toBehandling() = mapper.readValue(this, BehandlingSkjemaV2::class.java)
}
