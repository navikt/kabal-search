package no.nav.klage.search.clients.egenansatt

/*
@Component
class EgenAnsattHealthIndicator : HealthIndicator {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    private var kafkaConsumerHasReadAllMsgs = false


    @EventListener(condition = "event.listenerId.startsWith('klageEgenAnsattListener-')")
    fun eventHandler(event: ListenerContainerIdleEvent) {
        if (!kafkaConsumerHasReadAllMsgs) {
            logger.debug("Mottok ListenerContainerIdleEvent fra klageEgenAnsattListener")
        }
        kafkaConsumerHasReadAllMsgs = true
    }

    override fun health(): Health =
        if (kafkaConsumerHasReadAllMsgs) {
            Health.up().build()
        } else {
            Health.down().build()
        }
}
*/