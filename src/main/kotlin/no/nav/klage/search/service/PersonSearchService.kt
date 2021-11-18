package no.nav.klage.search.service

import no.nav.klage.search.clients.pdl.graphql.PdlClient
import no.nav.klage.search.clients.pdl.graphql.SoekPersonResponse
import no.nav.klage.search.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.personsoek.Navn
import no.nav.klage.search.domain.personsoek.Person
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class PersonSearchService(
    private val pdlClient: PdlClient, //TODO: Burde bruke PdlFacade ?
    private val elasticsearchService: ElasticsearchService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun fnrSearch(input: KlagebehandlingerSearchCriteria): PersonSearchResponse? {
        val searchHitsInES = esSoek(input)
        logger.debug("fnrSearch: Got ${searchHitsInES.size} hits from ES")
        val listOfPersonSoekResponse = searchHitsInES.groupBy { it.sakenGjelderFnr }.map { (fnr, klagebehandlinger) ->
            PersonSearchResponse(
                fnr = fnr!!,
                fornavn = klagebehandlinger.first().sakenGjelderFornavn
                    ?: throw RuntimeException("fornavn missing"),
                mellomnavn = klagebehandlinger.first().sakenGjelderMellomnavn,
                etternavn = klagebehandlinger.first().sakenGjelderEtternavn
                    ?: throw RuntimeException("etternavn missing"),
                klagebehandlinger = klagebehandlinger
            )
        }
        return if (listOfPersonSoekResponse.size == 1) {
            listOfPersonSoekResponse.first()
        } else if (listOfPersonSoekResponse.isEmpty()) {
            null
        } else {
            secureLogger.error("More than one hit for fnr {}.", input.foedselsnr)
            throw RuntimeException("More than one hit for fnr.")
        }
    }

    fun nameSearch(name: String): List<Person> {
        val pdlResponse = pdlClient.personsok(name)
        secureLogger.debug("Fetched data from PDL søk: $pdlResponse")
        verifyPdlResponse(pdlResponse)

        val people = pdlResponse.data?.sokPerson?.hits?.map { personHit ->
            Person(
                fnr = personHit.person.folkeregisteridentifikator.first().identifikasjonsnummer,
                navn = Navn(
                    fornavn = personHit.person.navn.first().fornavn,
                    mellomnavn = personHit.person.navn.first().mellomnavn,
                    etternavn = personHit.person.navn.first().etternavn
                )
            )
        }
        return people ?: emptyList()
    }

    private fun esSoek(input: KlagebehandlingerSearchCriteria): List<EsKlagebehandling> {
        val esResponse = elasticsearchService.findByCriteria(input)
        return esResponse.searchHits.map { it.content }
    }

    private fun verifyPdlResponse(response: SoekPersonResponse) {
        if (response.errors != null) {
            logger.error("Error from PDL, see secure logs")
            secureLogger.error("Error from pdl ${response.errors}")
            throw RuntimeException("Søkefeil i PDL")
        }
    }

    private fun SoekPersonResponse.collectFnr(): List<String> =
        this.data?.sokPerson?.hits?.map {
            it.person.folkeregisteridentifikator.first().identifikasjonsnummer
        } ?: listOf()
}
