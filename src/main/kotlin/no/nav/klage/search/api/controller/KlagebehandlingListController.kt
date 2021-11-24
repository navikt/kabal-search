package no.nav.klage.search.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.search.api.mapper.KlagebehandlingListMapper
import no.nav.klage.search.api.mapper.KlagebehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.AntallUtgaatteFristerResponse
import no.nav.klage.search.api.view.KlagebehandlingerListRespons
import no.nav.klage.search.api.view.KlagebehandlingerQueryParams
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.exceptions.NotMatchingUserException
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.SaksbehandlerService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["kabal-search"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
//TODO: Er det litt merkelig med "ansatte" på rot i path her?
@RequestMapping("/ansatte")
class KlagebehandlingListController(
    private val klagebehandlingListMapper: KlagebehandlingListMapper,
    private val elasticsearchService: ElasticsearchService,
    private val klagebehandlingerSearchCriteriaMapper: KlagebehandlingerSearchCriteriaMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerService: SaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent oppgaver for en ansatt",
        notes = "Henter alle oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/{navIdent}/klagebehandlinger", produces = ["application/json"])
    fun getOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: KlagebehandlingerQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        validateRettigheter(queryParams, navIdent)

        val valgtEnhet = getEnhetOrThrowException(queryParams.enhet)
        val searchCriteria = if (queryParams.temaer.isEmpty()) {
            klagebehandlingerSearchCriteriaMapper.toSearchCriteria(
                navIdent,
                queryParams.copy(
                    ytelser = valgtEnhet.ytelser.map { it.id }),
                valgtEnhet.enhet
            )
        } else {
            klagebehandlingerSearchCriteriaMapper.toSearchCriteria(navIdent, queryParams, valgtEnhet.enhet)
        }

        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esResponse.searchHits.map { it.content },
                searchCriteria.isProjectionUtvidet(),
                searchCriteria.ferdigstiltFom != null,
                searchCriteria.saksbehandler,
                valgtEnhet.ytelser
            )
        )
    }

    /*
        Does user have the rights to get all tildelte oppgaver?
     */
    private fun validateRettigheter(
        queryParams: KlagebehandlingerQueryParams,
        navIdent: String
    ) {
        if (queryParams.erTildeltSaksbehandler == true && queryParams.tildeltSaksbehandler == null) {
            if (!canSeeTildelteOppgaver()) {
                val message = "$navIdent har ikke tilgang til å se alle tildelte oppgaver."
                logger.warn(message)
                throw MissingTilgangException(message)
            }
        }
    }

    private fun canSeeTildelteOppgaver() = innloggetSaksbehandlerRepository.isLeder() ||
            innloggetSaksbehandlerRepository.isFagansvarlig() || true


    @ApiOperation(
        value = "Hent antall utildelte klagebehandlinger der fristen gått ut",
        notes = "Teller opp alle utildelte klagebehandlinger der fristen gått ut."
    )
    @GetMapping("/{navIdent}/antallklagebehandlingermedutgaattefrister", produces = ["application/json"])
    fun getAntallUtgaatteFrister(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: KlagebehandlingerQueryParams
    ): AntallUtgaatteFristerResponse {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)
        return AntallUtgaatteFristerResponse(
            antall = elasticsearchService.countByCriteria(
                klagebehandlingerSearchCriteriaMapper.toFristUtgaattIkkeTildeltSearchCriteria(
                    navIdent,
                    queryParams
                )
            )
        )
    }

    private fun validateNavIdent(navIdent: String) {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }
    }

    private fun getEnhetOrThrowException(enhetId: String): EnhetMedLovligeYtelser =
        saksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter.find { it.enhet.enhetId == enhetId }
            ?: throw IllegalArgumentException("Saksbehandler har ikke tilgang til angitt enhet")
}

