package no.nav.klage.search.clients.klageendret

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.search.service.IndexService
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.stereotype.Component


@Component
class KlageEndretKafkaConsumer(
    private val indexService: IndexService,
) : ConsumerSeekAware {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

    private lateinit var assignedTopicPartitions: List<TopicPartition>
    private lateinit var consumerSeekCallback: ConsumerSeekAware.ConsumerSeekCallback

    fun consumeFromBeginning() {
        logger.info("Seeking to beginning of ${assignedTopicPartitions.size} topic partitions")
        assignedTopicPartitions.forEach { topicPartition ->
            logger.info("Seeking to beginning of topic ${topicPartition.topic()} and partition ${topicPartition.partition()}")
            consumerSeekCallback.seekToBeginning(topicPartition.topic(), topicPartition.partition())
        }
    }

    override fun onPartitionsAssigned(
        assignments: MutableMap<TopicPartition, Long>,
        callback: ConsumerSeekAware.ConsumerSeekCallback
    ) {
        logger.debug("onPartitionsAssigned. Number of assignments are ${assignments.size}")
        this.assignedTopicPartitions = assignments.map { it.key }
    }

    override fun registerSeekCallback(callback: ConsumerSeekAware.ConsumerSeekCallback) {
        logger.debug("registerSeekCallback")
        this.consumerSeekCallback = callback
    }

    override fun onIdleContainer(
        assignments: MutableMap<TopicPartition, Long>?,
        callback: ConsumerSeekAware.ConsumerSeekCallback?
    ) {
        logger.debug("onIdleContainer. Number of assignments are ${assignments?.size}")
    }

    @KafkaListener(
        id = "klageEndretListener",
        idIsGroup = false,
        topics = ["klage.klage-endret.v1"],
        containerFactory = "klageEndretKafkaListenerContainerFactory",
    )
    fun listen(record: ConsumerRecord<String, String>) {
        runCatching {
            logger.debug("Reading offset ${record.offset()} from partition ${record.partition()} on kafka topic ${record.topic()}")
            val klagebehandlingId = record.key()
            logger.debug("Read klagebehandling with id $klagebehandlingId")
            val klagebehandling = record.value().toKlagebehandling()
            indexService.indexKlagebehandling(klagebehandling)
            logger.debug("Successfully indexed klagebehandling with id $klagebehandlingId")
        }.onFailure {
            secureLogger.error("Failed to process endret klage record", it)
            throw RuntimeException("Could not process endret klage record. See more details in secure log.")
        }
    }

    private fun String.toKlagebehandling() = mapper.readValue(this, KlagebehandlingSkjemaV1::class.java)
}
