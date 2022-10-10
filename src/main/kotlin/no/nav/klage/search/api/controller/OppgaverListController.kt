package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
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
@Tag(name = "kabal-search")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaverListController(
    private val behandlingListMapper: BehandlingListMapper,
    private val elasticsearchService: ElasticsearchService,
    private val behandlingerSearchCriteriaMapper: BehandlingerSearchCriteriaMapper,
    private val oAuthTokenService: OAuthTokenService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent ledige oppgaver for en saksbehandler",
        description = "Henter alle ledige oppgaver saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver/ledige", produces = ["application/json"])
    fun getMineLedigeOppgaver(
        queryParams: MineLedigeOppgaverQueryParams
    ): BehandlingerListRespons {
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
        val searchCriteria = behandlingerSearchCriteriaMapper.toLedigeOppgaverSearchCriteria(
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findLedigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = false,
            ),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = false,
            )
        )
    }

    @Operation(
        summary = "Hent ferdigstilte oppgaver for en ansatt",
        description = "Henter alle ferdigstilte oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver/ferdigstilte", produces = ["application/json"])
    fun getMineFerdigstilteOppgaver(
        @Parameter(name = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: MineFerdigstilteOppgaverQueryParams
    ): BehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        val ytelser = lovligeValgteYtelser(
            queryParams = queryParams,
            valgteEnheter = innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter
        )

        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = behandlingerSearchCriteriaMapper.toSaksbehandlersFerdigstilteOppgaverSearchCriteria(
            navIdent = navIdent,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findSaksbehandlersFerdigstilteOppgaverByCriteria(searchCriteria)
        return BehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = true,
            ),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = true,
            ),
        )
    }

    private fun emptyResponse(): BehandlingerListRespons =
        BehandlingerListRespons(antallTreffTotalt = 0, klagebehandlinger = emptyList(), behandlinger = emptyList())

    @Operation(
        summary = "Hent uferdige oppgaver for en ansatt",
        description = "Henter alle uferdige oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver/uferdige", produces = ["application/json"])
    fun getMineUferdigeOppgaver(
        @Parameter(name = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: MineUferdigeOppgaverQueryParams
    ): BehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        val ytelser = lovligeValgteYtelser(
            queryParams = queryParams,
            valgteEnheter = innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter
        )

        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = behandlingerSearchCriteriaMapper.toSaksbehandlersUferdigeOppgaverSearchCriteria(
            navIdent = navIdent,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findSaksbehandlersUferdigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = true,
            ),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = true,
            ),
        )
    }

    @Operation(
        summary = "Hent oppgaver satt på vent for en ansatt",
        description = "Henter alle oppgaver satt på vent som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver/paavent", produces = ["application/json"])
    fun getMineOppgaverPaaVent(
        @Parameter(name = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: MineOppgaverPaaVentQueryParams
    ): BehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        val ytelser = lovligeValgteYtelser(
            queryParams = queryParams,
            valgteEnheter = innloggetSaksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter
        )

        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = behandlingerSearchCriteriaMapper.toSaksbehandlersOppgaverPaaVentSearchCriteria(
            navIdent = navIdent,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findSaksbehandlersOppgaverPaaVentByCriteria(searchCriteria)
        return BehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = true,
            ),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
                visePersonData = true,
            ),
        )
    }

    @Operation(
        summary = "Hent enhetens ferdigstilte oppgaver",
        description = "Henter alle ferdigstilte oppgaver for enheten som saksbehandler har tilgang til."
    )
    @GetMapping("/enhet/{enhetId}/oppgaver/tildelte/ferdigstilte", produces = ["application/json"])
    fun getEnhetensFerdigstilteOppgaver(
        @Parameter(name = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensFerdigstilteOppgaverQueryParams
    ): BehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser = lovligeValgteYtelser(queryParams = queryParams, valgteEnheter = listOf(valgtEnhet))
        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = behandlingerSearchCriteriaMapper.toEnhetensFerdigstilteOppgaverSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findEnhetensFerdigstilteOppgaverByCriteria(searchCriteria)
        return BehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = behandlingListMapper.mapAnonymeEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
            behandlinger = behandlingListMapper.mapAnonymeEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent enhetens oppgaver på vent",
        description = "Henter alle oppgaver satt på vent for enheten som saksbehandler har tilgang til."
    )
    @GetMapping("/enhet/{enhetId}/oppgaver/tildelte/paavent", produces = ["application/json"])
    fun getEnhetensOppgaverPaaVent(
        @Parameter(name = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensOppgaverPaaVentQueryParams
    ): BehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser = lovligeValgteYtelser(queryParams = queryParams, valgteEnheter = listOf(valgtEnhet))
        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = behandlingerSearchCriteriaMapper.toEnhetensOppgaverPaaVentSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findEnhetensOppgaverPaaVentByCriteria(searchCriteria)
        return BehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = behandlingListMapper.mapAnonymeEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
            behandlinger = behandlingListMapper.mapAnonymeEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent uferdige oppgaver for en enhet",
        description = "Henter alle uferdige oppgaver i enheten som saksbehandler har tilgang til."
    )
    @GetMapping("/enhet/{enhetId}/oppgaver/tildelte/uferdige", produces = ["application/json"])
    fun getEnhetensUferdigeOppgaver(
        @Parameter(name = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensUferdigeOppgaverQueryParams
    ): BehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser = lovligeValgteYtelser(queryParams = queryParams, valgteEnheter = listOf(valgtEnhet))
        //TODO: Dette hadde vært bedre å håndtere i ElasticsearchService enn her
        if (ytelser.isEmpty()) {
            return emptyResponse()
        }

        //val hjemler: List<String> = lovligeValgteHjemler(queryParams = queryParams, ytelser = ytelser)
        val searchCriteria = behandlingerSearchCriteriaMapper.toEnhetensUferdigeOppgaverSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findEnhetensUferdigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = behandlingListMapper.mapAnonymeEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
            behandlinger = behandlingListMapper.mapAnonymeEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }


    @Operation(
        summary = "Hent antall utildelte behandlinger for enheten der fristen gått ut",
        description = "Teller opp alle utildelte behandlinger for enheten der fristen gått ut."
    )
    @GetMapping("/ansatte/{navIdent}/antalloppgavermedutgaattefrister", produces = ["application/json"])
    fun getAntallUtgaatteFrister(
        @Parameter(name = "NavIdent til en ansatt")
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
                criteria = behandlingerSearchCriteriaMapper.toSearchCriteriaForLedigeMedUtgaattFrist(
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

