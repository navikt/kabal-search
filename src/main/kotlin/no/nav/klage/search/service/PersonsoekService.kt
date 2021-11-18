package no.nav.klage.search.service

import no.nav.klage.search.clients.pdl.graphql.PdlClient
import no.nav.klage.search.clients.pdl.graphql.SoekPersonResponse
import no.nav.klage.search.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.personsoek.Navn
import no.nav.klage.search.domain.personsoek.Person
import no.nav.klage.search.domain.personsoek.PersonSoekResponse
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class PersonsoekService(
    private val pdlClient: PdlClient, //TODO: Burde bruke PdlFacade ?
    private val elasticsearchService: ElasticsearchService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun fnrSearch(input: KlagebehandlingerSearchCriteria): List<PersonSoekResponse> {
        val searchHits = esSoek(input)
        logger.debug("Personsøk with fnr: Got ${searchHits.size} hits from ES")
        val listOfPersonSoekResponse = searchHits.groupBy { it.sakenGjelderFnr }.map { (key, value) ->
            PersonSoekResponse(
                fnr = key!!,
                navn = value.first().sakenGjelderNavn,
                foedselsdato = null,
                klagebehandlinger = value
            )
        }
        return listOfPersonSoekResponse
    }

    fun nameSearch(name: String): List<Person> {
        val pdlResponse = pdlClient.personsok(name)
        secureLogger.debug("Fetched data from PDL søk: $pdlResponse")
        verifyPdlResponse(pdlResponse)

        val people = pdlResponse.data?.sokPerson?.hits?.map { personHit ->
            Person(
                fnr = personHit.person.folkeregisteridentifikator.first().identifikasjonsnummer,
                name = personHit.person.navn.first().toString(),
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
