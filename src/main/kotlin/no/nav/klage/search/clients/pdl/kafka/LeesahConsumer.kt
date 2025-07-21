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
        containerFactory = "egenAnsattKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(
            topic = "\${LEESAH_KAFKA_TOPIC}",
            partitions = ["#{@leesahFinder.partitions('\${LEESAH_KAFKA_TOPIC}')}"],
            partitionOffsets = [PartitionOffset(partition = "*", initialOffset = "0")]
        )]
    )
    fun listen(
        cr: ConsumerRecord<String, GenericRecord>,
        acknowledgment: Acknowledgment
    ) {
        if (cr.offset() == 1368199L) {
            val record = cr.value()
            logger.debug("Fant adressebeskyttelse hendelse, går videre.")
            logger.debug(
                "Reading offset {} from partition {} on kafka topic {}, {}",
                cr.offset(),
                cr.partition(),
                cr.topic(),
                record
            )
            logger.debug("Key: ${cr.key()}")
            logger.debug("fnr: {}", record.fnr)
            logger.debug("personidenter: {}", record.personidenter)
            logger.debug("opplysningstype: ${record.opplysningstype}")
        }

//        processPersonhendelse(
//            cr.value(),
//            cr.timestamp(),
//        )

//        acknowledgment.acknowledge()
    }

    fun processPersonhendelse(
        personhendelse: GenericRecord,
        timestamp: Long,
    ) {
        logger.debug("Processing personhendelse.")
        teamLogger.debug("Personhendelse received with type: ${personhendelse.opplysningstype}")
    }
}