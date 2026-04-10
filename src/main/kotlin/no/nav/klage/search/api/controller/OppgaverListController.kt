package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.*
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.OppgaverService
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "kabal-search")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaverListController(
    private val behandlingListMapper: BehandlingListMapper,
    private val elasticsearchService: ElasticsearchService,
    private val behandlingerSearchCriteriaMapper: BehandlingerSearchCriteriaMapper,
    private val oppgaverService: OppgaverService,
    private val tokenUtil: TokenUtil,
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
        queryParams: MineLedigeOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)
        return oppgaverService.getLedigeOppgaverForInnloggetSaksbehandler(queryParams = queryParams)
    }

    @Operation(
        summary = "Hent ferdigstilte oppgaver for en ansatt",
        description = "Henter alle ferdigstilte oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/oppgaver/ferdigstilte", produces = ["application/json"])
    fun getMineFerdigstilteOppgaver(
        queryParams: MineFerdigstilteOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toFerdigstilteOppgaverSearchCriteria(
            navIdent = tokenUtil.getIdent(),
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findSaksbehandlersFerdigstilteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
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
        queryParams: MineUferdigeOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toUferdigeOppgaverSearchCriteria(
            navIdent = tokenUtil.getIdent(),
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findSaksbehandlersUferdigeOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
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
        queryParams: MineOppgaverPaaVentQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toOppgaverPaaVentSearchCriteria(
            navIdent = tokenUtil.getIdent(),
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findSaksbehandlersOppgaverPaaVentByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
        )
    }

    @Operation(
        summary = "Hent antall utildelte behandlinger tilgjengelig for saksbehandler der fristen gått ut",
        description = "Hent antall utildelte behandlinger tilgjengelig for saksbehandler der fristen gått ut"
    )
    @GetMapping("/antalloppgavermedutgaattefrister", produces = ["application/json"])
    fun getUtgaatteFristerAvailableToSaksbehandlerCount(
        queryParams: MineLedigeOppgaverCountQueryParams
    ): AntallUtgaatteFristerResponse {
        logger.debug("Params: {}", queryParams)
        return oppgaverService.getUtgaatteFristerAvailableToSaksbehandlerCount(queryParams = queryParams)
    }
}