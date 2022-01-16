package no.nav.klage.search.repositories

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.klageenhetTilYtelser
import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.search.domain.saksbehandler.EnheterMedLovligeYtelser
import no.nav.klage.search.gateway.AxsysGateway
import no.nav.klage.search.gateway.AzureGateway
import no.nav.klage.search.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SaksbehandlerRepository(
    private val azureGateway: AzureGateway,
    private val axsysGateway: AxsysGateway,
    @Value("\${ROLE_KLAGE_SAKSBEHANDLER}") private val saksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_FAGANSVARLIG}") private val fagansvarligRole: String,
    @Value("\${ROLE_KLAGE_LEDER}") private val lederRole: String,
    @Value("\${ROLE_KLAGE_MERKANTIL}") private val merkantilRole: String,
    @Value("\${ROLE_KLAGE_FORTROLIG}") private val kanBehandleFortroligRole: String,
    @Value("\${ROLE_KLAGE_STRENGT_FORTROLIG}") private val kanBehandleStrengtFortroligRole: String,
    @Value("\${ROLE_KLAGE_EGEN_ANSATT}") private val kanBehandleEgenAnsattRole: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        val saksbehandlerNameCache = mutableMapOf<String, String>()

        const val MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY = 15
    }

    fun harTilgangTilEnhetOgYtelse(ident: String, enhetId: String, ytelse: Ytelse): Boolean {
        return getEnheterMedYtelserForSaksbehandler(ident).enheter.firstOrNull { it.enhet.enhetId == enhetId }?.ytelser?.contains(
            ytelse
        ) ?: false
    }

    fun harTilgangTilEnhet(ident: String, enhetId: String): Boolean {
        return getEnheterMedYtelserForSaksbehandler(ident).enheter.firstOrNull { it.enhet.enhetId == enhetId } != null
    }

    fun harTilgangTilYtelse(ident: String, ytelse: Ytelse): Boolean {
        return getEnheterMedYtelserForSaksbehandler(ident).enheter.flatMap { it.ytelser }.contains(ytelse)
    }

    fun getEnheterMedYtelserForSaksbehandler(ident: String): EnheterMedLovligeYtelser =
        axsysGateway.getEnheterForSaksbehandler(ident).berikMedYtelser()
    //listOf(azureGateway.getDataOmInnloggetSaksbehandler().enhet).berikMedYtelser()

    private fun List<Enhet>.berikMedYtelser(): EnheterMedLovligeYtelser {
        return EnheterMedLovligeYtelser(this.map {
            EnhetMedLovligeYtelser(
                enhet = it,
                ytelser = getYtelserForEnhet(it)
            )
        })
    }

    private fun getYtelserForEnhet(enhet: Enhet): List<Ytelse> =
        klageenhetTilYtelser.filter { it.key.navn == enhet.enhetId }.flatMap { it.value }


    fun getNamesForSaksbehandlere(identer: Set<String>): Map<String, String> {
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
}