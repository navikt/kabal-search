package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.*
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.OppgaverService
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
    private val oppgaverService: OppgaverService,
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
        summary = "Hent oppgave",
        description = "Hent oppgave."
    )
    @GetMapping("/oppgaver/{behandlingId}", produces = ["application/json"])
    fun getOppgave(
        @PathVariable behandlingId: String,
    ): BehandlingView {
        logger.debug("getOppgave: {}", behandlingId)

        val searchCriteria = behandlingerSearchCriteriaMapper.toBehandlingIdSearchCriteria(
            behandlingId = behandlingId,
        )
        val esResponse = elasticsearchService.findOppgaveByBehandlingId(searchCriteria)

        val result = behandlingListMapper.mapEsBehandlingerToBehandlingView(
            esBehandlinger = esResponse.searchHits.map { it.content },
        )

        return result.first()
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
            navIdent = oAuthTokenService.getInnloggetIdent(),
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
        summary = "Hent ferdigstilte ROL-oppgaver for en ansatt",
        description = "Henter alle ferdigstilte ROL-oppgaver som ROL har tilgang til."
    )
    @GetMapping("/roloppgaver/ferdigstilte", produces = ["application/json"])
    fun getMineFerdigstilteROLOppgaver(
        queryParams: MineFerdigstilteOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toFerdigstilteOppgaverSearchCriteria(
            navIdent = oAuthTokenService.getInnloggetIdent(),
            queryParams = queryParams
        )

        val esResponse = elasticsearchService.findROLsFerdigstilteOppgaverByCriteria(searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            ),
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
        summary = "Hent uferdige oppgaver for en ansatt",
        description = "Henter alle uferdige oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/oppgaver/uferdige", produces = ["application/json"])
    fun getMineUferdigeOppgaver(
        queryParams: MineUferdigeOppgaverQueryParams
    ): BehandlingerListResponse {
        logger.debug("Params: {}", queryParams)

        val searchCriteria = behandlingerSearchCriteriaMapper.toUferdigeOppgaverSearchCriteria(
            navIdent = oAuthTokenService.getInnloggetIdent(),
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
            navIdent = oAuthTokenService.getInnloggetIdent(),
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