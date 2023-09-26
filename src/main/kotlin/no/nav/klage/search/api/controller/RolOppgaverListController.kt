package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.BehandlingerListResponse
import no.nav.klage.search.api.view.MineLedigeOppgaverQueryParams
import no.nav.klage.search.api.view.MineReturnerteROLOppgaverQueryParams
import no.nav.klage.search.api.view.MineUferdigeOppgaverQueryParams
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "kabal-search")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class RolOppgaverListController(
    private val behandlingListMapper: BehandlingListMapper,
    private val elasticsearchService: ElasticsearchService,
    private val behandlingerSearchCriteriaMapper: BehandlingerSearchCriteriaMapper,
    private val oAuthTokenService: OAuthTokenService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent ledige ROL-oppgaver",
        description = "Henter alle ledige ROL-oppgaver."
    )
    @GetMapping("/roloppgaver/ledige", produces = ["application/json"])
    fun getLedigeRolOppgaver(
        queryParams: MineLedigeOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toLedigeOppgaverSearchCriteria(
            queryParams = queryParams,
        )

        val esResponse = elasticsearchService.findLedigeROLOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            )
        )
    }

    @Operation(
        summary = "Hent returnert ROL-oppgaver for en ansatt",
        description = "Henter alle returnerte ROL-oppgaver som ROL har tilgang til."
    )
    @GetMapping("/roloppgaver/returnerte", produces = ["application/json"])
    fun getMineReturnerteROLOppgaver(
        queryParams: MineReturnerteROLOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toReturnerteROLOppgaverSearchCriteria(
            navIdent = oAuthTokenService.getInnloggetIdent(),
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findROLsReturnerteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent uferdige ROL-oppgaver for en ansatt",
        description = "Henter alle uferdige ROL-oppgaver som ROL har tilgang til."
    )
    @GetMapping("/roloppgaver/uferdige", produces = ["application/json"])
    fun getMineUferdigeROLOppgaver(
        queryParams: MineUferdigeOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toUferdigeOppgaverSearchCriteria(
            navIdent = oAuthTokenService.getInnloggetIdent(),
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findROLsUferdigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

}