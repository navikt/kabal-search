package no.nav.klage.search.service

import no.nav.klage.search.clients.pdl.graphql.PdlClient
import no.nav.klage.search.clients.pdl.graphql.SoekPersonResponse
import no.nav.klage.search.domain.OppgaverOmPersonSearchCriteria
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.personsoek.Navn
import no.nav.klage.search.domain.personsoek.Person
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getTeamLogger
import org.springframework.stereotype.Service

@Service
class PersonSearchService(
    private val pdlClient: PdlClient, //TODO: Burde bruke PdlFacade ?
    private val elasticsearchService: ElasticsearchService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    fun fnrSearch(input: OppgaverOmPersonSearchCriteria): PersonSearchResponse? {
        val searchHitsInES = esSoek(input)
        logger.debug("fnrSearch: Got ${searchHitsInES.size} hits from ES")
        return if (searchHitsInES.isEmpty()) {
            null
        } else {
            PersonSearchResponse(
                behandlinger = searchHitsInES
            )
        }
    }

    fun nameSearch(name: String): List<Person> {
        val pdlResponse = pdlClient.personsok(name)
        verifyPdlResponse(pdlResponse)

        val people = pdlResponse.data?.sokPerson?.hits
            ?.filter { personHit -> personHit.person.folkeregisteridentifikator.isNotEmpty() }
            ?.map { personHit ->
                Person(
                    fnr = personHit.person.folkeregisteridentifikator.first().identifikasjonsnummer,
                    navn = Navn(
                        fornavn = personHit.person.navn.first().fornavn,
                        mellomnavn = personHit.person.navn.firstOrNull()?.mellomnavn,
                        etternavn = personHit.person.navn.first().etternavn
                    )
                )
            }
        return people ?: emptyList()
    }

    private fun esSoek(input: OppgaverOmPersonSearchCriteria): List<EsBehandling> {
        val esResponse = elasticsearchService.findOppgaverOmPersonByCriteria(input)
        return esResponse.searchHits.map { it.content }
    }

    private fun verifyPdlResponse(response: SoekPersonResponse) {
        if (response.errors != null) {
            logger.error("Error from PDL, see team-logs for details")
            teamLogger.error("Error from pdl ${response.errors}")
            throw RuntimeException("Søkefeil i PDL")
        }
    }
}
