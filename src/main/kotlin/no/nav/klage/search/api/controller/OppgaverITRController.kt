package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.BehandlingerListResponse
import no.nav.klage.search.api.view.EnhetensOppgaverPaaVentQueryParams
import no.nav.klage.search.api.view.EnhetensUferdigeOppgaverQueryParams
import no.nav.klage.search.api.view.FerdigstilteOppgaverQueryParams
import no.nav.klage.search.api.view.OppgaverPaaVentQueryParams
import no.nav.klage.search.api.view.UferdigeOppgaverQueryParams
import no.nav.klage.search.clients.klagelookup.KlageLookupClient
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
        summary = "Hent alle ferdigstilte oppgaver, default oppgaver i Trygderetten",
        description = "Henter alle ferdigstilte oppgaver, default oppgaver i Trygderetten."
    )
    @GetMapping(
        "/oppgaver-i-tr/ferdigstilte",
        produces = ["application/json"]
    )
    fun getFerdigstilteOppgaver(
        queryParams: FerdigstilteOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForOppgaverITR()

        val searchCriteria = behandlingerSearchCriteriaMapper.toFerdigstilteOppgaverSearchCriteria(
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findFerdigstilteOppgaverByCriteria(searchCriteria)
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
        "/oppgaver-i-tr/paavent",
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

    @Operation(
        summary = "Hent alle uferdige oppgaver, default oppgaver i Trygderetten",
        description = "Hent alle uferdige oppgaver, default oppgaver i Trygderetten",
    )
    @GetMapping(
        "/oppgaver-i-tr/uferdige",
        produces = ["application/json"]
    )
    fun getUferdigeOppgaver(
        queryParams: UferdigeOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForOppgaverITR()

        val searchCriteria = behandlingerSearchCriteriaMapper.toUferdigeOppgaverSearchCriteria(
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findUferdigeOppgaverByCriteria(searchCriteria)
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