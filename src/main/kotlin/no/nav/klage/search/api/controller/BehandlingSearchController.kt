package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.*
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.personsoek.Navn
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
    @PostMapping("/oppgaver", produces = ["application/json"])
    fun findOppgaver(@RequestBody input: SearchPersonByFnrInput): FnrSearchResponseWithoutPerson {
        val personSearchResponse =
            personSearchService.fnrSearch(behandlingerSearchCriteriaMapper.toOppgaverOmPersonSearchCriteria(input))

        return if (personSearchResponse == null) {
            FnrSearchResponseWithoutPerson(
                aapneBehandlinger = listOf(),
                avsluttedeBehandlinger = listOf(),
                feilregistrerteBehandlinger = listOf(),
                paaVentBehandlinger = listOf(),
            )
        } else {
            behandlingListMapper.mapPersonSearchResponseToFnrSearchResponseWithoutPerson(
                personSearchResponse = personSearchResponse,
            )
        }
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
                    id = it.fnr,
                    name = it.navn.toFullName()
                )
            }
        )
    }

    private fun Navn.toFullName(): String {
        return if (mellomnavn != null) {
            "$fornavn $mellomnavn $etternavn"
        } else {
            "$fornavn $etternavn"
        }
    }
}

