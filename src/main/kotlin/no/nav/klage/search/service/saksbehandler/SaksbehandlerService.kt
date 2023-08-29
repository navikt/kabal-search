package no.nav.klage.search.service.saksbehandler

import no.nav.klage.search.api.view.SaksbehandlereListResponse
import no.nav.klage.search.gateway.AzureGateway
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SaksbehandlerService(
    private val azureGateway: AzureGateway,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        val saksbehandlerNameCache = mutableMapOf<String, String>()

        const val MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY = 15
    }

    private fun expandAndReturnSaksbehandlerNameCache(identer: Set<String>): Map<String, String> {
        logger.debug("Fetching names for saksbehandlere from Microsoft Graph")

        val identerNotInCache = identer.toMutableSet()
        identerNotInCache -= saksbehandlerNameCache.keys
        logger.debug("Only fetching identer not in cache: {}", identerNotInCache)

        val chunkedList = identerNotInCache.chunked(MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY)

        val measuredTimeMillis = measureTimeMillis {
            saksbehandlerNameCache += azureGateway.getAllDisplayNames(chunkedList)
        }
        logger.debug("It took {} millis to fetch all names", measuredTimeMillis)

        return saksbehandlerNameCache
    }

    fun getNameForIdent(it: String) =
        expandAndReturnSaksbehandlerNameCache(setOf(it)).getOrDefault(it, "Ukjent navn")

    fun getSaksbehandlereForEnhet(enhetsnummer: String): List<SaksbehandlereListResponse.SaksbehandlerView> {
        val azureOutput = azureGateway.getEnhetensAnsattesNavIdentsWithKabalSaksbehandlerRole(enhetsnummer = enhetsnummer)
        logger.debug("azureOutput: {}", azureOutput)
        return azureOutput.value?.map {
            SaksbehandlereListResponse.SaksbehandlerView(
                navIdent = it.onPremisesSamAccountName,
                navn = it.displayName,
            )
        } ?: emptyList()
    }
}
