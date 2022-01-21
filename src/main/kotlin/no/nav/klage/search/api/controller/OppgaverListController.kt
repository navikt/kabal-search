package no.nav.klage.search.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.api.mapper.KlagebehandlingListMapper
import no.nav.klage.search.api.mapper.KlagebehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.*
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.kodeverk.ytelseTilSoekehjemler
import no.nav.klage.search.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.exceptions.NotMatchingUserException
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.saksbehandler.InnloggetSaksbehandlerService
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["kabal-search"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaverListController(
    private val klagebehandlingListMapper: KlagebehandlingListMapper,
    private val elasticsearchService: ElasticsearchService,
    private val klagebehandlingerSearchCriteriaMapper: KlagebehandlingerSearchCriteriaMapper,
    private val oAuthTokenService: OAuthTokenService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent ledige oppgaver for en saksbehandler",
        notes = "Henter alle ledige oppgaver saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver/ledige", produces = ["application/json"])
    fun getMineLedigeOppgaver(
        queryParams: MineLedigeOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)

        val ytelser = lovligeValgteYtelser(
            queryParams = queryParams,
            valgteEnheter = innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter
        )
        //TODO: Dette hadde vært bedre å håndtere i ElasticsearchService enn her
        if (ytelser.isEmpty()) {
            return emptyResponse()
        }

        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toLedigeOppgaverSearchCriteria(
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findLedigeOppgaverByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esKlagebehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = false,
                saksbehandlere = emptyList(),
                tilgangTilYtelser = getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }

    @ApiOperation(
        value = "Hent ferdigstilte oppgaver for en ansatt",
        notes = "Henter alle ferdigstilte oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver/ferdigstilte", produces = ["application/json"])
    fun getMineFerdigstilteOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: MineFerdigstilteOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        val ytelser = lovligeValgteYtelser(
            queryParams = queryParams,
            valgteEnheter = innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter
        )

        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSaksbehandlersFerdigstilteOppgaverSearchCriteria(
            navIdent = navIdent,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findSaksbehandlersFerdigstilteOppgaverByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esKlagebehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = true,
                saksbehandlere = listOf(searchCriteria.saksbehandler),
                tilgangTilYtelser = getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }

    private fun emptyResponse(): KlagebehandlingerListRespons =
        KlagebehandlingerListRespons(antallTreffTotalt = 0, klagebehandlinger = emptyList())

    @ApiOperation(
        value = "Hent uferdige oppgaver for en ansatt",
        notes = "Henter alle uferdige oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver/uferdige", produces = ["application/json"])
    fun getMineUferdigeOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: MineUferdigeOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        val ytelser = lovligeValgteYtelser(
            queryParams = queryParams,
            valgteEnheter = innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter
        )

        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSaksbehandlersUferdigeOppgaverSearchCriteria(
            navIdent = navIdent,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findSaksbehandlersUferdigeOppgaverByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esKlagebehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = true,
                saksbehandlere = listOf(searchCriteria.saksbehandler),
                tilgangTilYtelser = getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }

    @ApiOperation(
        value = "Hent enhetens ferdigstilte oppgaver",
        notes = "Henter alle ferdigstilte oppgaver for enheten som saksbehandler har tilgang til."
    )
    @GetMapping("/enhet/{enhetId}/oppgaver/tildelte/ferdigstilte", produces = ["application/json"])
    fun getEnhetensFerdigstilteOppgaver(
        @ApiParam(value = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensFerdigstilteOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser = lovligeValgteYtelser(queryParams = queryParams, valgteEnheter = listOf(valgtEnhet))
        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toEnhetensFerdigstilteOppgaverSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findEnhetensFerdigstilteOppgaverByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esKlagebehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = false,
                saksbehandlere = searchCriteria.saksbehandlere,
                tilgangTilYtelser = getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }

    @ApiOperation(
        value = "Hent uferdige oppgaver for en enhet",
        notes = "Henter alle uferdige oppgaver i enheten som saksbehandler har tilgang til."
    )
    @GetMapping("/enhet/{enhetId}/oppgaver/tildelte/uferdige", produces = ["application/json"])
    fun getEnhetensUferdigeOppgaver(
        @ApiParam(value = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensUferdigeOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser = lovligeValgteYtelser(queryParams = queryParams, valgteEnheter = listOf(valgtEnhet))
        //TODO: Dette hadde vært bedre å håndtere i ElasticsearchService enn her
        if (ytelser.isEmpty()) {
            return emptyResponse()
        }

        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toEnhetensUferdigeOppgaverSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findEnhetensUferdigeOppgaverByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esKlagebehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = false,
                saksbehandlere = searchCriteria.saksbehandlere,
                tilgangTilYtelser = getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }


    @ApiOperation(
        value = "Hent antall utildelte klagebehandlinger for enheten der fristen gått ut",
        notes = "Teller opp alle utildelte klagebehandlinger for enheten der fristen gått ut."
    )
    @GetMapping("/ansatte/{navIdent}/antalloppgavermedutgaattefrister", produces = ["application/json"])
    fun getAntallUtgaatteFrister(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: MineLedigeOppgaverQueryParams
    ): AntallUtgaatteFristerResponse {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        val ytelser = lovligeValgteYtelser(
            queryParams = queryParams,
            valgteEnheter = innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter
        )
        //TODO: Dette hadde vært bedre å håndtere i ElasticsearchService enn her
        if (ytelser.isEmpty()) {
            return AntallUtgaatteFristerResponse(0)
        }
        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        return AntallUtgaatteFristerResponse(
            antall = elasticsearchService.countLedigeOppgaverMedUtgaatFristByCriteria(
                criteria = klagebehandlingerSearchCriteriaMapper.toSearchCriteriaForLedigeMedUtgaattFrist(
                    navIdent = navIdent,
                    queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
                )
            )
        )
    }

    private fun validateNavIdent(navIdent: String) {
        val innloggetIdent = oAuthTokenService.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }
    }

    private fun getEnhetOrThrowException(enhetId: String): EnhetMedLovligeYtelser =
        innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter.find { it.enhet.enhetId == enhetId }
            ?: throw IllegalArgumentException("Saksbehandler har ikke tilgang til angitt enhet")

    private fun validateRettigheterForEnhetensTildelteOppgaver() {
        if (!(oAuthTokenService.isLeder() || oAuthTokenService.isFagansvarlig())) {
            val message =
                "${oAuthTokenService.getInnloggetIdent()} har ikke tilgang til å se alle tildelte oppgaver."
            logger.warn(message)
            throw MissingTilgangException(message)
        }
    }

    private fun lovligeValgteYtelser(
        queryParams: CommonOppgaverQueryParams,
        valgteEnheter: List<EnhetMedLovligeYtelser>
    ) =
        if (queryParams.ytelser.isEmpty()) {
            valgteEnheter.flatMap { it.ytelser }.map { it.id }
        } else {
            valgteEnheter.flatMap { it.ytelser }.map { it.id }.intersect(queryParams.ytelser)
        }.toList()

    private fun lovligeValgteHjemler(queryParams: CommonOppgaverQueryParams, ytelser: List<String>) =
        if (queryParams.hjemler.isEmpty()) {
            ytelser.mapNotNull { ytelseTilSoekehjemler.get(Ytelse.of(it)) }.flatten().map { it.id }
        } else {
            ytelser.mapNotNull { ytelseTilSoekehjemler.get(Ytelse.of(it)) }.flatten().map { it.id }
                .intersect(queryParams.hjemler).toList()
        }

    private fun getAlleYtelserInnloggetSaksbehandlerKanBehandle() =
        innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter.flatMap { it.ytelser }
}

