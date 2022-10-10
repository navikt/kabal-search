package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.*
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import no.nav.klage.search.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.search.exceptions.PersonNotFoundException
import no.nav.klage.search.service.PersonSearchService
import no.nav.klage.search.service.saksbehandler.InnloggetSaksbehandlerService
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
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
    private val oAuthTokenService: OAuthTokenService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
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
        val saksbehandler = oAuthTokenService.getInnloggetIdent()

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

    // Not in use atm
/*    @PostMapping("/relaterte")
    fun getRelaterteKlagebehandlinger(
        @RequestBody input: SearchPersonByFnrInput
    ): KlagebehandlingerListRespons {
        //TODO: Move logic to PersonsoekService
        val lovligeTemaer = getEnhetOrThrowException(input.enhet).temaer
        val sivilstand: Sivilstand? = pdlFacade.getPersonInfo(input.query).sivilstand

        val searchCriteria = KlagebehandlingerSearchCriteria(
            statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.ALLE,
            ferdigstiltFom = LocalDate.now().minusMonths(12),
            foedselsnr = listOf(input.query),
            extraPersonAndTema = sivilstand?.let {
                KlagebehandlingerSearchCriteria.ExtraPersonAndTema(
                    foedselsnr = it.foedselsnr,
                    temaer = TemaTilgjengeligeForEktefelle.temaerTilgjengeligForEktefelle(environment).toList()
                )
            },
            offset = 0,
            limit = 100,
            projection = KlagebehandlingerSearchCriteria.Projection.UTVIDET,
        )

        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esKlagebehandlinger = esResponse.searchHits.map { it.content },
                viseUtvidet = true,
                viseFullfoerte = true,
                saksbehandler = searchCriteria.saksbehandler,
                tilgangTilTemaer = lovligeTemaer,
                sivilstand = sivilstand
            )
        )
    }*/

    private fun getEnhetOrThrowException(enhetId: String): EnhetMedLovligeYtelser =
        innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter.find { it.enhet.enhetId == enhetId }
            ?: throw IllegalArgumentException("Saksbehandler har ikke tilgang til angitt enhet")

}

