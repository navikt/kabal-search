package no.nav.klage.search.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.search.api.view.SaksbehandlereListResponse
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.SaksbehandlereByEnhetSearchCriteria
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.SaksbehandlerService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["kabal-search"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class SaksbehandlerController(
    private val elasticsearchService: ElasticsearchService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerService: SaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent saksbehandlere i gitt enhet",
        notes = "Henter alle saksbehandlere fra aktive saker i gitt enhet."
    )
    @GetMapping("/enheter/{enhet}/saksbehandlere", produces = ["application/json"])
    fun getSaksbehandlereForEnhet(
        @ApiParam(value = "Enhet")
        @PathVariable enhet: String
    ): SaksbehandlereListResponse {
        logger.debug("getSaksbehandlereForEnhet")

        if (!saksbehandlerService.hasSaksbehandlerAccessToEnhet(enhet)) {
            throw MissingTilgangException("Saksbehandler ${innloggetSaksbehandlerRepository.getInnloggetIdent()} does not have access to enhet $enhet")
        }

        val esResponse = elasticsearchService.findSaksbehandlereByEnhetCriteria(
            SaksbehandlereByEnhetSearchCriteria(
                enhet = enhet,
                kanBehandleEgenAnsatt = innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt(),
                kanBehandleFortrolig = innloggetSaksbehandlerRepository.kanBehandleFortrolig(),
                kanBehandleStrengtFortrolig = innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig(),
            )
        )
        return SaksbehandlereListResponse(
            saksbehandlere = esResponse.map {
                SaksbehandlereListResponse.SaksbehandlerView(
                    navIdent = it.navIdent,
                    navn = it.navn
                )
            }
        )
    }

}

