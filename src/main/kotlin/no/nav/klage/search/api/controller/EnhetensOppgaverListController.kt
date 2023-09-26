package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.BehandlingerListResponse
import no.nav.klage.search.api.view.EnhetensFerdigstilteOppgaverQueryParams
import no.nav.klage.search.api.view.EnhetensOppgaverPaaVentQueryParams
import no.nav.klage.search.api.view.EnhetensUferdigeOppgaverQueryParams
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "kabal-search")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class EnhetensOppgaverListController(
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
        "/enhet/{enhetId}/oppgaver/tildelte/ferdigstilte", "/enheter/{enhetId}/oppgaver/tildelte/ferdigstilte",
        produces = ["application/json"]
    )
    fun getEnhetensFerdigstilteOppgaver(
        @Parameter(name = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensFerdigstilteOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val searchCriteria = behandlingerSearchCriteriaMapper.toEnhetensFerdigstilteOppgaverSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findEnhetensFerdigstilteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent enhetens oppgaver på vent",
        description = "Henter alle oppgaver satt på vent for enheten som saksbehandler har tilgang til."
    )
    @GetMapping(
        "/enhet/{enhetId}/oppgaver/tildelte/paavent", "/enheter/{enhetId}/oppgaver/tildelte/paavent",
        produces = ["application/json"]
    )
    fun getEnhetensOppgaverPaaVent(
        @Parameter(name = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensOppgaverPaaVentQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val searchCriteria = behandlingerSearchCriteriaMapper.toEnhetensOppgaverPaaVentSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findEnhetensOppgaverPaaVentByCriteria(searchCriteria)
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
        "/enhet/{enhetId}/oppgaver/tildelte/uferdige",
        produces = ["application/json"]
    )
    fun getEnhetensUferdigeOppgaver(
        @Parameter(name = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensUferdigeOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val searchCriteria = behandlingerSearchCriteriaMapper.toEnhetensUferdigeOppgaverSearchCriteria(
            enhetId = enhetId,
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findEnhetensUferdigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
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
}