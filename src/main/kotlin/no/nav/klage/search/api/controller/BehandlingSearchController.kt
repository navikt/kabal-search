package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.*
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import no.nav.klage.search.exceptions.PersonNotFoundException
import no.nav.klage.search.service.PersonSearchService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/search")
@Tag(name = "kabal-search")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class BehandlingSearchController(
    private val behandlingListMapper: BehandlingListMapper,
    private val behandlingerSearchCriteriaMapper: BehandlingerSearchCriteriaMapper,
    private val personSearchService: PersonSearchService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Søk oppgaver som gjelder en gitt person",
        description = "Finner alle oppgaver som saksbehandler har tilgang til som omhandler en gitt person."
    )
    @PostMapping("/personogoppgaver", produces = ["application/json"])
    fun getPersonOgOppgaver(@RequestBody input: SearchPersonByFnrInput): FnrSearchResponse {
        val personSearchResponse: PersonSearchResponse =
            personSearchService.fnrSearch(behandlingerSearchCriteriaMapper.toOppgaverOmPersonSearchCriteria(input))
                ?: throw PersonNotFoundException("Person med fnr ${input.query} ikke funnet")

        return behandlingListMapper.mapPersonSearchResponseToFnrSearchResponse(
            personSearchResponse = personSearchResponse,
        )
    }

    @Operation(
        summary = "Hent oppgaver som gjelder en gitt person",
        description = "Henter alle oppgaver som saksbehandler har tilgang til som omhandler en gitt person."
    )
    @PostMapping("/name", produces = ["application/json"])
    fun getNameSearchResponse(@RequestBody input: SearchPersonByNameInput): NameSearchResponse {
        val people = personSearchService.nameSearch(input.query)
        return NameSearchResponse(
            people = people.map {
                NameSearchResponse.PersonView(
                    fnr = it.fnr,
                    navn = NavnView(
                        fornavn = it.navn.fornavn,
                        mellomnavn = it.navn.mellomnavn,
                        etternavn = it.navn.etternavn
                    )
                )
            }
        )
    }
}

