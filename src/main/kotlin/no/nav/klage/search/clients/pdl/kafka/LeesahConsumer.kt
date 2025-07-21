package no.nav.klage.search.clients.pdl.kafka

import no.nav.klage.search.clients.pdl.PdlFacade
import no.nav.klage.search.clients.pdl.PersonCacheService
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getTeamLogger
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class LeesahConsumer(
    private val personCacheService: PersonCacheService,
    private val pdlFacade: PdlFacade,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    @KafkaListener(
        id = "klageSearchLeesahListener",
        idIsGroup = true,
        containerFactory = "leesahKafkaListenerContainerFactory",
        topics = ["\${LEESAH_KAFKA_TOPIC}"],
        properties = ["auto.offset.reset = earliest"],
    )
    fun listen(
        cr: ConsumerRecord<String, GenericRecord>,
    ) {
        processPersonhendelse(
            personhendelse = cr.value(),
        )
    }

    fun processPersonhendelse(
        personhendelse: GenericRecord,
    ) {
        if (personhendelse.isAdressebeskyttelse) {
            logger.debug("Found adressebeskyttelse event. Checking if person is cached.")
            if (personCacheService.isCached(foedselsnr = personhendelse.fnr)) {
                logger.debug("Found fnr in person cache. Removing person from cache.")
                personCacheService.removePerson(foedselsnr = personhendelse.fnr)
                logger.debug("Updating cache.")
                pdlFacade.getPersonInfo(fnr = personhendelse.fnr)
            }
        }
    }
}