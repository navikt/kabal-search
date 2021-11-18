package no.nav.klage.search.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.nav.klage.search.api.mapper.KlagebehandlingListMapper
import no.nav.klage.search.api.mapper.KlagebehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.*
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.saksbehandler.EnhetMedLovligeTemaer
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.service.PersonSearchService
import no.nav.klage.search.service.SaksbehandlerService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/search")
@Api(tags = ["kabal-search"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingSearchController(
    private val klagebehandlingListMapper: KlagebehandlingListMapper,
    private val klagebehandlingerSearchCriteriaMapper: KlagebehandlingerSearchCriteriaMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerService: SaksbehandlerService,
    private val personSearchService: PersonSearchService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Søk oppgaver som gjelder en gitt person",
        notes = "Finner alle oppgaver som saksbehandler har tilgang til som omhandler en gitt person."
    )
    @PostMapping("/fnr", produces = ["application/json"])
    fun getFnrSearchResponse(@RequestBody input: SearchPersonByFnrInput): FnrSearchResponse? {
        val personSearchResponse =
            personSearchService.fnrSearch(klagebehandlingerSearchCriteriaMapper.toSearchCriteria(input))

        return if (personSearchResponse != null) {
            val saksbehandler = innloggetSaksbehandlerRepository.getInnloggetIdent()
            val valgtEnhet = getEnhetOrThrowException(input.enhet)
            klagebehandlingListMapper.mapPersonSearchResponseToFnrSearchResponse(
                personSearchResponse = personSearchResponse,
                saksbehandler = saksbehandler,
                tilgangTilTemaer = valgtEnhet.temaer
            )
        } else {
            null
        }
    }

    @ApiOperation(
        value = "Hent oppgaver som gjelder en gitt person",
        notes = "Henter alle oppgaver som saksbehandler har tilgang til som omhandler en gitt person."
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

    private fun getEnhetOrThrowException(enhetId: String): EnhetMedLovligeTemaer =
        saksbehandlerService.getEnheterMedTemaerForSaksbehandler().enheter.find { it.enhetId == enhetId }
            ?: throw IllegalArgumentException("Saksbehandler har ikke tilgang til angitt enhet")

}

