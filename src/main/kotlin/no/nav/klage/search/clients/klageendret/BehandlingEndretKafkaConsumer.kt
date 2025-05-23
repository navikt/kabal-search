package no.nav.klage.search.clients.klageendret

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.search.service.IndexService
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getTeamLogger
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
        private val teamLogger = getTeamLogger()
        private val mapper = ObjectMapper().registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        ).registerModule(JavaTimeModule())
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
            teamLogger.error("Failed to process endret behandling record", it)
            throw RuntimeException("Could not process endret behandling record. See more details in team-logs.")
        }
    }

    private fun String.toBehandling() = mapper.readValue(this, BehandlingSkjemaV2::class.java)
}
