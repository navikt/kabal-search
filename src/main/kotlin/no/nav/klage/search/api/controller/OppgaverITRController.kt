package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.BehandlingerListResponse
import no.nav.klage.search.api.view.FerdigstilteOppgaverITRQueryParams
import no.nav.klage.search.api.view.LedigeOppgaverITRQueryParams
import no.nav.klage.search.api.view.OppgaverPaaVentITRQueryParams
import no.nav.klage.search.api.view.TildelteOppgaverITRQueryParams
import no.nav.klage.search.clients.klagelookup.KlageLookupClient
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.KabalInnstillingerService
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "kabal-search")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaverITRController(
    private val behandlingListMapper: BehandlingListMapper,
    private val elasticsearchService: ElasticsearchService,
    private val behandlingerSearchCriteriaMapper: BehandlingerSearchCriteriaMapper,
    private val tokenUtil: TokenUtil,
    private val klageLookupClient: KlageLookupClient,
    private val kabalInnstillingerService: KabalInnstillingerService,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent alle tildelte oppgaver, default oppgaver i Trygderetten",
        description = "Henter alle tildelte oppgaver, default oppgaver i Trygderetten.",
    )
    @GetMapping(
        "/oppgaver-i-tr/tildelte",
        produces = ["application/json"],
    )
    fun getTildelteOppgaver(queryParams: TildelteOppgaverITRQueryParams): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForOppgaverITR()

        val tilgjengeligeYtelser = getTilgjengeligeYtelser(queryParams.ytelser)
        if (tilgjengeligeYtelser.isEmpty()) {
            return tomtResultat()
        }
        queryParams.ytelser = tilgjengeligeYtelser

        val searchCriteria =
            behandlingerSearchCriteriaMapper.toTildelteOppgaverSearchCriteria(
                queryParams = queryParams,
            )

        val esResponse = elasticsearchService.findTildelteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger =
                behandlingListMapper.mapEsBehandlingerToListView(
                    esBehandlinger = esResponse.searchHits.map { it.content },
                ),
        )
    }

    @Operation(
        summary = "Hent alle ledige oppgaver, default oppgaver i Trygderetten",
        description = "Hent alle ledige oppgaver, default oppgaver i Trygderetten",
    )
    @GetMapping(
        "/oppgaver-i-tr/ledige",
        produces = ["application/json"],
    )
    fun getLedigeOppgaver(queryParams: LedigeOppgaverITRQueryParams): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForOppgaverITR()

        val tilgjengeligeYtelser = getTilgjengeligeYtelser(queryParams.ytelser)
        if (tilgjengeligeYtelser.isEmpty()) {
            return tomtResultat()
        }
        queryParams.ytelser = tilgjengeligeYtelser

        val searchCriteria =
            behandlingerSearchCriteriaMapper.toLedigeOppgaverSearchCriteria(
                queryParams = queryParams,
            )

        val esResponse = elasticsearchService.findLedigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger =
                behandlingListMapper.mapEsBehandlingerToListView(
                    esBehandlinger = esResponse.searchHits.map { it.content },
                ),
        )
    }

    @Operation(
        summary = "Hent alle oppgaver på vent, default oppgaver i Trygderetten",
        description = "Hent alle oppgaver på vent, default oppgaver i Trygderetten",
    )
    @GetMapping(
        "/oppgaver-i-tr/paa-vent",
        produces = ["application/json"],
    )
    fun getOppgaverPaaVent(queryParams: OppgaverPaaVentITRQueryParams): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForOppgaverITR()

        val tilgjengeligeYtelser = getTilgjengeligeYtelser(queryParams.ytelser)
        if (tilgjengeligeYtelser.isEmpty()) {
            return tomtResultat()
        }
        queryParams.ytelser = tilgjengeligeYtelser

        val searchCriteria =
            behandlingerSearchCriteriaMapper.toOppgaverPaaVentSearchCriteria(
                queryParams = queryParams,
            )

        val esResponse = elasticsearchService.findOppgaverPaaVentByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger =
                behandlingListMapper.mapEsBehandlingerToListView(
                    esBehandlinger = esResponse.searchHits.map { it.content },
                ),
        )
    }

    @Operation(
        summary = "Hent alle ferdigstilte oppgaver, default oppgaver i Trygderetten",
        description = "Hent alle ferdigstilte oppgaver, default oppgaver i Trygderetten",
    )
    @GetMapping(
        "/oppgaver-i-tr/ferdigstilte",
        produces = ["application/json"],
    )
    fun getFerdigstilteOppgaver(queryParams: FerdigstilteOppgaverITRQueryParams): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForOppgaverITR()

        val tilgjengeligeYtelser = getTilgjengeligeYtelser(queryParams.ytelser)
        if (tilgjengeligeYtelser.isEmpty()) {
            return tomtResultat()
        }
        queryParams.ytelser = tilgjengeligeYtelser

        val searchCriteria =
            behandlingerSearchCriteriaMapper.toFerdigstilteOppgaverSearchCriteria(
                queryParams = queryParams,
            )

        val esResponse = elasticsearchService.findFerdigstilteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger =
                behandlingListMapper.mapEsBehandlingerToListView(
                    esBehandlinger = esResponse.searchHits.map { it.content },
                ),
        )
    }

    private fun getTilgjengeligeYtelser(queryYtelser: List<String>): List<String> {
        val ytelserForSaksbehandler =
            kabalInnstillingerService
                .getSaksbehandlersAccess(navIdent = tokenUtil.getIdent())
                .ytelser
                .map { it.id }

        return if (queryYtelser.isEmpty()) {
            ytelserForSaksbehandler
        } else {
            ytelserForSaksbehandler.intersect(queryYtelser.toSet()).toList()
        }
    }

    private fun tomtResultat(): BehandlingerListResponse =
        BehandlingerListResponse(
            antallTreffTotalt = 0,
            behandlinger = emptyList(),
        )

    private fun validateRettigheterForOppgaverITR() {
        val navIdent = tokenUtil.getIdent()
        if (!klageLookupClient.getUserGroups(navIdent = navIdent).groups.contains(AzureGroup.KABAL_OPPGAVESTYRING_ALLE_ENHETER)) {
            val message =
                "$navIdent har ikke tilgang til å se alle tildelte oppgaver."
            logger.warn(message)
            throw MissingTilgangException(message)
        }
    }
}
