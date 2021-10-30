package no.nav.klage.search.clients.pdl

import no.nav.klage.search.clients.pdl.graphql.*
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.stereotype.Component

@Component
class PdlFacade(
    private val pdlClient: PdlClient,
    private val personCacheService: PersonCacheService,
    private val hentPersonMapper: HentPersonMapper
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun getPersonerInfo(fnrListe: List<String>): List<Person> {
        val fnrPartition: Pair<List<String>, List<String>> =
            fnrListe.partition { personCacheService.isCached(it) }

        val fnrIsCached = fnrPartition.first
        val cachedPersoner = fnrIsCached.map { personCacheService.getPerson(it) }

        val fnrIsNotCached = fnrPartition.second
        if (fnrIsNotCached.isNotEmpty()) {
            val hentPersonBolkResult = pdlClient.getPersonerInfo(fnrIsNotCached).getHentPersonBolkAndLogErrors()
            val newPersoner = hentPersonMapper.mapToPersoner(hentPersonBolkResult)
            oppdaterCache(newPersoner)
            return cachedPersoner + newPersoner
        }
        return cachedPersoner

        //Evt. enklere men tregere versjon:
        // fnrListe.map { getPersonInfo(it) }
    }

    private fun oppdaterCache(newPersoner: List<Person>) {
        newPersoner.forEach { personCacheService.updatePersonCache(it) }
    }

    fun getPersonInfo(fnr: String): Person {
        if (personCacheService.isCached(fnr)) {
            return personCacheService.getPerson(fnr)
        }
        val hentPersonResponse: HentPersonResponse = pdlClient.getPersonInfo(fnr)
        val pdlPerson = hentPersonResponse.getPersonOrLogErrors(fnr)
        return hentPersonMapper.mapToPerson(fnr, pdlPerson).also { personCacheService.updatePersonCache(it) }
    }

    private fun HentPersonResponse.getPersonOrLogErrors(fnr: String): PdlPerson =
        if (this.errors.isNullOrEmpty() && this.data != null && this.data.hentPerson != null) {
            this.data.hentPerson
        } else {
            logger.warn("Errors returned from PDL or person not found. See securelogs for details.")
            secureLogger.warn("Errors returned for hentPerson($fnr) from PDL: ${this.errors}")
            throw RuntimeException("Klarte ikke å hente person fra PDL")
        }

    private fun HentPersonerResponse.getHentPersonBolkAndLogErrors(): List<HentPersonBolkResult> {
        if (!this.errors.isNullOrEmpty()) {
            logger.warn("Errors returned from PDL. See securelogs for details.")
            secureLogger.warn("Errors returned from PDL: ${this.errors}")
        }
        return this.data?.hentPersonBolk ?: emptyList()
    }
}