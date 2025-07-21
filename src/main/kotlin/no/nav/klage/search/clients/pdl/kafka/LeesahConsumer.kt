package no.nav.klage.search.clients.pdl.kafka

import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getTeamLogger
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class LeesahConsumer {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    @KafkaListener(
        id = "klageSearchLeesahListener",
        idIsGroup = false,
        containerFactory = "leesahKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(
            topic = "\${LEESAH_KAFKA_TOPIC}",
            partitions = ["#{@leesahFinder.partitions('\${LEESAH_KAFKA_TOPIC}')}"],
            partitionOffsets = [PartitionOffset(partition = "*", initialOffset = "0")]
        )]
    )
    fun listen(cr: ConsumerRecord<String, GenericRecord>,
               acknowledgment: Acknowledgment,) {
        processPersonhendelse(
            cr.value(),
            cr.timestamp(),
        )

        acknowledgment.acknowledge()
    }

    fun processPersonhendelse(
        personhendelse: GenericRecord,
        timestamp: Long,
    ) {
        teamLogger.debug("Personhendelse received with type: ${personhendelse.opplysningstype}")
    }
}