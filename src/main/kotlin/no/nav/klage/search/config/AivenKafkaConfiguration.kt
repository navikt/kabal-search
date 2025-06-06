package no.nav.klage.search.config

import no.nav.klage.search.util.getLogger
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.CommonLoggingErrorHandler
import org.springframework.kafka.listener.ContainerProperties.AckMode
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.util.backoff.FixedBackOff
import java.io.Serializable
import java.time.Duration


@Configuration
class AivenKafkaConfiguration(
    @Value("\${KAFKA_BROKERS}")
    private val kafkaBrokers: String,
    @Value("\${KAFKA_TRUSTSTORE_PATH}")
    private val kafkaTruststorePath: String,
    @Value("\${KAFKA_CREDSTORE_PASSWORD}")
    private val kafkaCredstorePassword: String,
    @Value("\${KAFKA_KEYSTORE_PATH}")
    private val kafkaKeystorePath: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //Consumer beans
    @Bean
    fun egenAnsattKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = egenAnsattConsumerFactory()
        factory.containerProperties.ackMode = AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.idleEventInterval = 3000L
        factory.setCommonErrorHandler(CommonLoggingErrorHandler())

        //Retry consumer/listener even if authorization fails at first
        factory.setContainerCustomizer { container ->
            container.containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(10L))
        }

        return factory
    }

    @Bean
    fun behandlingEndretKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = klageEndretConsumerFactory()

        factory.containerProperties.ackMode = AckMode.RECORD
        factory.setCommonErrorHandler(DefaultErrorHandler(FixedBackOff(1000L, 3L)))
        factory.containerProperties.idleEventInterval = 3000L

        //Retry consumer/listener even if authorization fails at first
        factory.setContainerCustomizer { container ->
            container.containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(10L))
        }

        return factory
    }

    @Bean
    fun egenAnsattConsumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(getConsumerProps())
    }

    @Bean
    fun klageEndretConsumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(getConsumerProps())
    }

    private fun getConsumerProps(): Map<String, Serializable> {
        return mapOf(
            ConsumerConfig.GROUP_ID_CONFIG to "kabal-search",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            "spring.deserializer.key.delegate.class" to StringDeserializer::class.java,
            "spring.deserializer.value.delegate.class" to StringDeserializer::class.java
        ) + commonConfig()
    }

    @Bean
    fun egenAnsattFinder(): PartitionFinder<String, String> {
        return PartitionFinder(egenAnsattConsumerFactory())
    }

    //Common
    private fun commonConfig() = mapOf(
        BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers
    ) + securityConfig()

    private fun securityConfig() = mapOf(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
        SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "", // Disable server host name verification
        SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to kafkaTruststorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to kafkaCredstorePassword,
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to kafkaKeystorePath,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to kafkaCredstorePassword,
        SslConfigs.SSL_KEY_PASSWORD_CONFIG to kafkaCredstorePassword,
    )

}

class PartitionFinder<K, V>(private val consumerFactory: ConsumerFactory<K, V>) {
    fun partitions(topic: String): Array<String> {
        consumerFactory.createConsumer().use { consumer ->
            return consumer.partitionsFor(topic)
                .map { pi -> "" + pi.partition() }
                .toTypedArray()
        }
    }
}
