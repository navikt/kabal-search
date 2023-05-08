package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.*
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
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
class OppgaverListControllerNew(
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
    @GetMapping("/oppgaver/ledige", produces = ["application/json"])
    fun getMineLedigeOppgaver(
        queryParams: MineLedigeOppgaverQueryParamsNew
    ): BehandlingerListResponsNew {
        logger.debug("Params: {}", queryParams)

        val ytelser = getYtelserQueryListForSaksbehandler(
            queryParams = queryParams,
            tildelteYtelser = innloggetSaksbehandlerService.getTildelteYtelserForSaksbehandler()
        )
        //TODO: Dette hadde vært bedre å håndtere i ElasticsearchService enn her
        if (ytelser.isEmpty()) {
            return BehandlingerListResponsNew(antallTreffTotalt = 0, behandlinger = emptyList())
        }

        val searchCriteria = behandlingerSearchCriteriaMapper.toLedigeOppgaverSearchCriteria(
            queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findLedigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponsNew(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListViewNew(
                esBehandlinger = esResponse.searchHits.map { it.content },
            )
        )
    }

    @Operation(
        summary = "Hent oppgave",
        description = "Hent oppgave."
    )
    @GetMapping("/oppgaver/{behandlingId}", produces = ["application/json"])
    fun getOppgave(
        @PathVariable behandlingId: String,
    ): BehandlingListView {
        logger.debug("getOppgave: {}", behandlingId)

        val searchCriteria = behandlingerSearchCriteriaMapper.toBehandlingIdSearchCriteria(
            behandlingId = behandlingId,
        )
        val esResponse = elasticsearchService.findOppgaveByBehandlingId(searchCriteria)

        val result = behandlingListMapper.mapEsBehandlingerToListView(
            esBehandlinger = esResponse.searchHits.map { it.content },
            visePersonData = false,
        )

        return result.first()
    }

    @Operation(
        summary = "Hent person",
        description = "Hent person."
    )
    @GetMapping("/oppgaver/{behandlingId}/person", produces = ["application/json"])
    fun getPersonByOppgave(
        @PathVariable behandlingId: String,
    ): PersonView {
        logger.debug("getPersonByOppgave: {}", behandlingId)

        val searchCriteria = behandlingerSearchCriteriaMapper.toBehandlingIdSearchCriteria(
            behandlingId = behandlingId,
        )
        val esResponse = elasticsearchService.findOppgaveByBehandlingId(searchCriteria)

        val result = behandlingListMapper.mapEsBehandlingerToListView(
            esBehandlinger = esResponse.searchHits.map { it.content },
            visePersonData = true,
        )

        return result.first().person!!
    }

    @Operation(
        summary = "Hent ferdigstilte oppgaver for en ansatt",
        description = "Henter alle ferdigstilte oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/oppgaver/ferdigstilte", produces = ["application/json"])
    fun getMineFerdigstilteOppgaver(
        queryParams: MineFerdigstilteOppgaverQueryParamsNew
    ): BehandlingerListResponsNew {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toSaksbehandlersFerdigstilteOppgaverSearchCriteria(
            navIdent = oAuthTokenService.getInnloggetIdent(),
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findSaksbehandlersFerdigstilteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponsNew(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListViewNew(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent uferdige oppgaver for en ansatt",
        description = "Henter alle uferdige oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/oppgaver/uferdige", produces = ["application/json"])
    fun getMineUferdigeOppgaver(
        queryParams: MineUferdigeOppgaverQueryParamsNew
    ): BehandlingerListResponsNew {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toSaksbehandlersUferdigeOppgaverSearchCriteria(
            navIdent = oAuthTokenService.getInnloggetIdent(),
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findSaksbehandlersUferdigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponsNew(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListViewNew(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent oppgaver satt på vent for en ansatt",
        description = "Henter alle oppgaver satt på vent som saksbehandler har tilgang til."
    )
    @GetMapping("/oppgaver/paavent", produces = ["application/json"])
    fun getMineOppgaverPaaVent(
        queryParams: MineOppgaverPaaVentQueryParamsNew
    ): BehandlingerListResponsNew {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toSaksbehandlersOppgaverPaaVentSearchCriteria(
            navIdent = oAuthTokenService.getInnloggetIdent(),
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findSaksbehandlersOppgaverPaaVentByCriteria(searchCriteria)
        return BehandlingerListResponsNew(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListViewNew(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent enhetens ferdigstilte oppgaver",
        description = "Henter alle ferdigstilte oppgaver for enheten som saksbehandler har tilgang til."
    )
    @GetMapping(
        value = ["/enhet/{enhetId}/oppgaver/tildelte/ferdigstilte_new", "/enhet/{enhetId}/oppgaver/tildelte/ferdigstilte"],
        produces = ["application/json"]
    )
    fun getEnhetensFerdigstilteOppgaver(
        @Parameter(name = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensFerdigstilteOppgaverQueryParamsNew
    ): BehandlingerListResponsNew {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val searchCriteria = behandlingerSearchCriteriaMapper.toEnhetensFerdigstilteOppgaverSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findEnhetensFerdigstilteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponsNew(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapAnonymeEsBehandlingerToListViewNew(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent enhetens oppgaver på vent",
        description = "Henter alle oppgaver satt på vent for enheten som saksbehandler har tilgang til."
    )
    @GetMapping(
        value = ["/enhet/{enhetId}/oppgaver/tildelte/paavent_new", "/enhet/{enhetId}/oppgaver/tildelte/paavent"],
        produces = ["application/json"]
    )
    fun getEnhetensOppgaverPaaVent(
        @Parameter(name = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensOppgaverPaaVentQueryParamsNew
    ): BehandlingerListResponsNew {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val searchCriteria = behandlingerSearchCriteriaMapper.toEnhetensOppgaverPaaVentSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findEnhetensOppgaverPaaVentByCriteria(searchCriteria)
        return BehandlingerListResponsNew(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapAnonymeEsBehandlingerToListViewNew(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent uferdige oppgaver for en enhet",
        description = "Henter alle uferdige oppgaver i enheten som saksbehandler har tilgang til."
    )
    @GetMapping(
        value = ["/enhet/{enhetId}/oppgaver/tildelte/uferdige_new", "/enhet/{enhetId}/oppgaver/tildelte/uferdige"],
        produces = ["application/json"]
    )
    fun getEnhetensUferdigeOppgaver(
        @Parameter(name = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensUferdigeOppgaverQueryParamsNew
    ): BehandlingerListResponsNew {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val searchCriteria = behandlingerSearchCriteriaMapper.toEnhetensUferdigeOppgaverSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findEnhetensUferdigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponsNew(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapAnonymeEsBehandlingerToListViewNew(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent antall utildelte behandlinger for tilgjengelig for saksbehandler der fristen gått ut",
        description = "Hent antall utildelte behandlinger for tilgjengelig for saksbehandler der fristen gått ut"
    )
    @GetMapping("/antalloppgavermedutgaattefrister", produces = ["application/json"])
    fun getUtgaatteFristerAvailableToSaksbehandlerCount(
        queryParams: MineLedigeOppgaverCountQueryParams
    ): AntallUtgaatteFristerResponse {
        logger.debug("Params: {}", queryParams)

        val ytelser = getYtelserQueryListForSaksbehandler(
            queryParams = queryParams,
            tildelteYtelser = innloggetSaksbehandlerService.getTildelteYtelserForSaksbehandler()
        )
        //TODO: Dette hadde vært bedre å håndtere i ElasticsearchService enn her
        if (ytelser.isEmpty()) {
            return AntallUtgaatteFristerResponse(0)
        }

        return AntallUtgaatteFristerResponse(
            antall = elasticsearchService.countLedigeOppgaverMedUtgaattFristByCriteria(
                criteria = behandlingerSearchCriteriaMapper.toSearchCriteriaForLedigeMedUtgaattFrist(
                    navIdent = oAuthTokenService.getInnloggetIdent(),
                    queryParams = queryParams.copy(ytelser = ytelser) //, hjemler = hjemler),
                )
            )
        )
    }

    private fun validateRettigheterForEnhetensTildelteOppgaver() {
        if (!oAuthTokenService.isKabalOppgavestyringEgenEnhet()) {
            val message =
                "${oAuthTokenService.getInnloggetIdent()} har ikke tilgang til å se alle tildelte oppgaver."
            logger.warn(message)
            throw MissingTilgangException(message)
        }
    }

    private fun getYtelserQueryListForSaksbehandler(
        queryParams: CommonOppgaverQueryParams,
        tildelteYtelser: List<Ytelse>
    ): List<String> {
        return if (queryParams.ytelser.isEmpty()) {
            tildelteYtelser.map { it.id }
        } else {
            tildelteYtelser.map { it.id }.intersect(queryParams.ytelser.toSet())
        }.toList()
    }
}