package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.AnketeamFerdigstilteOppgaverQueryParams
import no.nav.klage.search.api.view.AnketeamLedigeOppgaverQueryParams
import no.nav.klage.search.api.view.AnketeamOppgaverPaaVentQueryParams
import no.nav.klage.search.api.view.AnketeamUferdigeOppgaverQueryParams
import no.nav.klage.search.api.view.BehandlingerListResponse
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
class AnketeamOppgaverListController(
    private val behandlingListMapper: BehandlingListMapper,
    private val elasticsearchService: ElasticsearchService,
    private val behandlingerSearchCriteriaMapper: BehandlingerSearchCriteriaMapper,
    private val klageLookupClient: KlageLookupClient,
    private val tokenUtil: TokenUtil,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent anketeamets ferdigstilte oppgaver",
        description = "Henter alle ferdigstilte oppgaver for anketeamet.",
    )
    @GetMapping(
        "/anketeam/oppgaver/tildelte/ferdigstilte",
        produces = ["application/json"],
    )
    fun getAnketeamsFerdigstilteOppgaver(queryParams: AnketeamFerdigstilteOppgaverQueryParams): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForAnketeametsOppgaver()

        val searchCriteria =
            behandlingerSearchCriteriaMapper.toAnketeamFerdigstilteOppgaverSearchCriteria(
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

    @Operation(
        summary = "Hent anketeamets oppgaver på vent",
        description = "Henter alle oppgaver satt på vent for anketeamet.",
    )
    @GetMapping(
        "/anketeam/oppgaver/tildelte/paavent",
        produces = ["application/json"],
    )
    fun getAnketeamsOppgaverPaaVent(queryParams: AnketeamOppgaverPaaVentQueryParams): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForAnketeametsOppgaver()

        val searchCriteria =
            behandlingerSearchCriteriaMapper.toAnketeamOppgaverPaaVentSearchCriteria(
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
        summary = "Hent anketeamets uferdige oppgaver",
        description = "Henter alle uferdige oppgaver for anketeamet.",
    )
    @GetMapping(
        "/anketeam/oppgaver/tildelte/uferdige",
        produces = ["application/json"],
    )
    fun getAnketeamsUferdigeOppgaver(queryParams: AnketeamUferdigeOppgaverQueryParams): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForAnketeametsOppgaver()

        val searchCriteria =
            behandlingerSearchCriteriaMapper.toAnketeamUferdigeOppgaverSearchCriteria(
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
        summary = "Hent ledige oppgaver for anketeamet",
        description = "Henter alle ledige oppgaver for anketeamet som saksbehandler har tilgang til.",
    )
    @GetMapping(
        "/anketeam/oppgaver/ledige",
        produces = ["application/json"],
    )
    fun getAnketeamsLedigeOppgaver(queryParams: AnketeamLedigeOppgaverQueryParams): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForAnketeametsOppgaver()

        val searchCriteria =
            behandlingerSearchCriteriaMapper.toAnketeamLedigeOppgaverSearchCriteria(
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

    private fun validateRettigheterForAnketeametsOppgaver() {
        val navIdent = tokenUtil.getIdent()
        // TODO: Bytt til AzureGroup.KABAL_OPPGAVESTYRING_ANKETEAM når rollen finnes i klage-kodeverk og er opprettet i Azure.
        //  Fram til da brukes KABAL_INNSYN_EGEN_ENHET som stand-in.
        if (!klageLookupClient.getUserGroups(navIdent = navIdent).groups.contains(AzureGroup.KABAL_INNSYN_EGEN_ENHET)) {
            val message = "$navIdent har ikke tilgang til å se anketeamets oppgaver."
            logger.warn(message)
            throw MissingTilgangException(message)
        }
    }
}
