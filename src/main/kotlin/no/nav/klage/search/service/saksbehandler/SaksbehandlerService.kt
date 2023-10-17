package no.nav.klage.search.service.saksbehandler

import no.nav.klage.search.api.view.SaksbehandlerView
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

    fun getSaksbehandlereForEnhet(enhetsnummer: String): List<SaksbehandlerView> {
        val azureOutput = azureGateway.getEnhetensAnsattesNavIdentsWithKabalSaksbehandlerRole(enhetsnummer = enhetsnummer)
        return azureOutput.value?.map {
            SaksbehandlerView(
                navIdent = it.onPremisesSamAccountName,
                navn = it.displayName,
            )
        } ?: emptyList()
    }

    fun getROLListForEnhet(enhetsnummer: String): List<SaksbehandlerView> {
        val azureOutput = azureGateway.getEnhetensAnsattesNavIdentsWithKabalROLRole(enhetsnummer = enhetsnummer)
        return azureOutput.value?.map {
            SaksbehandlerView(
                navIdent = it.onPremisesSamAccountName,
                navn = it.displayName,
            )
        } ?: emptyList()
    }

    fun getEnhetsnummerForNavIdent(navIdent: String): String? = azureGateway.getEnhetsnummerForNavIdent(navIdent)

}
