package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.BehandlingerListResponse
import no.nav.klage.search.api.view.TildelteOppgaverQueryParams
import no.nav.klage.search.api.view.OppgaverPaaVentQueryParams
import no.nav.klage.search.api.view.LedigeOppgaverQueryParams
import no.nav.klage.search.clients.klagelookup.KlageLookupClient
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.service.ElasticsearchService
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
    private val klageLookupClient: KlageLookupClient,
    private val tokenUtil: TokenUtil
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent alle tildelte oppgaver, default oppgaver i Trygderetten",
        description = "Henter alle tildelte oppgaver, default oppgaver i Trygderetten."
    )
    @GetMapping(
        "/oppgaver-i-tr/tildelte",
        produces = ["application/json"]
    )
    fun getTildelteOppgaver(
        queryParams: TildelteOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForOppgaverITR()

        val searchCriteria = behandlingerSearchCriteriaMapper.toTildelteOppgaverSearchCriteria(
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findTildelteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
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
        produces = ["application/json"]
    )
    fun getLedigeOppgaver(
        queryParams: LedigeOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForOppgaverITR()

        val searchCriteria = behandlingerSearchCriteriaMapper.toLedigeOppgaverSearchCriteria(
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findLedigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent alle oppgaver på vent, default oppgaver i Trygderetten",
        description = "Hent alle oppgaver på vent, default oppgaver i Trygderetten"
    )
    @GetMapping(
        "/oppgaver-i-tr/paa-vent",
        produces = ["application/json"]
    )
    fun getOppgaverPaaVent(
        queryParams: OppgaverPaaVentQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForOppgaverITR()

        val searchCriteria = behandlingerSearchCriteriaMapper.toOppgaverPaaVentSearchCriteria(
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findOppgaverPaaVentByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

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