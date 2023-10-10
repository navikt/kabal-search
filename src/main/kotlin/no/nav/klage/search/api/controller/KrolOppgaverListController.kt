package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.BehandlingerListResponse
import no.nav.klage.search.api.view.KrolsReturnerteOppgaverQueryParams
import no.nav.klage.search.api.view.KrolsUferdigeOppgaverQueryParams
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "kabal-search")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KrolOppgaverListController(
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
        summary = "Hent enhetens ferdigstilte oppgaver",
        description = "Henter alle ferdigstilte oppgaver for enheten som saksbehandler har tilgang til."
    )
    @GetMapping(
        "/kroloppgaver/tildelte/returnerte",
        produces = ["application/json"]
    )
    fun getKrolsReturnerteOppgaver(
        queryParams: KrolsReturnerteOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForKrolOppgaver()

        val searchCriteria = behandlingerSearchCriteriaMapper.toKrolsReturnerteOppgaverSearchCriteria(
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findKrolsReturnerteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent uferdige oppgaver for en enhet",
        description = "Henter alle uferdige oppgaver i enheten som saksbehandler har tilgang til."
    )
    @GetMapping(
        "/kroloppgaver/tildelte/uferdige",
        produces = ["application/json"]
    )
    fun getKrolsUferdigeOppgaver(
        queryParams: KrolsUferdigeOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForKrolOppgaver()

        val searchCriteria = behandlingerSearchCriteriaMapper.toKrolsUferdigeOppgaverSearchCriteria(
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findKrolsUferdigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    private fun validateRettigheterForKrolOppgaver() {
        if (!oAuthTokenService.isKROL()) {
            val message =
                "${oAuthTokenService.getInnloggetIdent()} har ikke tilgang til å se alle oppgaver."
            logger.warn(message)
            throw MissingTilgangException(message)
        }
    }
}