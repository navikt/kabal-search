package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.search.api.view.MedunderskrivereListResponse
import no.nav.klage.search.api.view.ROLListResponse
import no.nav.klage.search.api.view.SaksbehandlerView
import no.nav.klage.search.api.view.SaksbehandlereListResponse
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.ROLListSearchCriteria
import no.nav.klage.search.domain.SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.saksbehandler.InnloggetSaksbehandlerService
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import no.nav.klage.search.service.saksbehandler.SaksbehandlerService
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "kabal-search")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class SaksbehandlerController(
    private val elasticsearchService: ElasticsearchService,
    private val oAuthTokenService: OAuthTokenService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val saksbehandlerService: SaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Operation(
        summary = "Hent saksbehandlere i gitt enhet",
        description = "Henter alle saksbehandlere fra aktive saker i gitt enhet."
    )
    @GetMapping("/enheter/{enhet}/saksbehandlere", produces = ["application/json"])
    fun getSaksbehandlereForEnhet(
        @Parameter(name = "Enhet")
        @PathVariable enhet: String
    ): SaksbehandlereListResponse {
        logger.debug("getSaksbehandlereForEnhet")

        if (innloggetSaksbehandlerService.getEnhetForSaksbehandler().enhetId != enhet) {
            throw MissingTilgangException("Saksbehandler ${oAuthTokenService.getInnloggetIdent()} does not have access to enhet $enhet")
        }

        val esResponse = elasticsearchService.findSaksbehandlereByEnhetCriteria(
            SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria(
                enhet = enhet,
                kanBehandleEgenAnsatt = oAuthTokenService.kanBehandleEgenAnsatt(),
                kanBehandleFortrolig = oAuthTokenService.kanBehandleFortrolig(),
                kanBehandleStrengtFortrolig = oAuthTokenService.kanBehandleStrengtFortrolig(),
            )
        )

        val saksbehandlereFromES = esResponse.map {
            SaksbehandlerView(
                navIdent = it.navIdent,
                navn = it.navn
            )
        }

        val saksbehandlereFromMSGraph = saksbehandlerService.getSaksbehandlereForEnhet(enhetsnummer = enhet)

        return SaksbehandlereListResponse(
            saksbehandlere = (saksbehandlereFromES + saksbehandlereFromMSGraph)
                .toSortedSet(compareBy { it.navn }).toList()
        )
    }

    @Operation(
        summary = "Hent medunderskrivere i gitt enhet",
        description = "Henter alle medunderskrivere fra aktive saker i gitt enhet."
    )
    @GetMapping("/enheter/{enhet}/medunderskrivere", produces = ["application/json"])
    fun getMedunderskrivereForEnhet(
        @Parameter(name = "Enhet")
        @PathVariable enhet: String
    ): MedunderskrivereListResponse {
        logger.debug("getMedunderskrivereForEnhet")

        if (innloggetSaksbehandlerService.getEnhetForSaksbehandler().enhetId != enhet) {
            throw MissingTilgangException("Saksbehandler ${oAuthTokenService.getInnloggetIdent()} does not have access to enhet $enhet")
        }

        val esResponse = elasticsearchService.findMedunderskrivereByEnhetCriteria(
            SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria(
                enhet = enhet,
                kanBehandleEgenAnsatt = oAuthTokenService.kanBehandleEgenAnsatt(),
                kanBehandleFortrolig = oAuthTokenService.kanBehandleFortrolig(),
                kanBehandleStrengtFortrolig = oAuthTokenService.kanBehandleStrengtFortrolig(),
            )
        )

        val saksbehandlereFromES = esResponse.map {
            SaksbehandlerView(
                navIdent = it.navIdent,
                navn = it.navn
            )
        }

        val saksbehandlereFromMSGraph = saksbehandlerService.getSaksbehandlereForEnhet(enhetsnummer = enhet)

        return MedunderskrivereListResponse(
            medunderskrivere = (saksbehandlereFromES + saksbehandlereFromMSGraph)
                .toSortedSet(compareBy { it.navn }).toList()
        )
    }

    @Operation(
        summary = "Hent ROL i gitt enhet",
        description = "Henter alle ROL fra aktive saker i gitt enhet."
    )
    //TODO: Remove old version when FE are ready.
    @GetMapping("/rol-list", "/enheter/{enhet}/rol-list", produces = ["application/json"])
    fun getRolList(
    ): ROLListResponse {
        logger.debug("getRolList")

        val esResponse = elasticsearchService.findROLListByEnhetCriteria(
            ROLListSearchCriteria(
                kanBehandleEgenAnsatt = oAuthTokenService.kanBehandleEgenAnsatt(),
                kanBehandleFortrolig = oAuthTokenService.kanBehandleFortrolig(),
                kanBehandleStrengtFortrolig = oAuthTokenService.kanBehandleStrengtFortrolig(),
            )
        )

        val rolListFromES = esResponse.map {
            SaksbehandlerView(
                navIdent = it.navIdent,
                navn = it.navn
            )
        }

        val rolListFromMSGraph = saksbehandlerService.getROLList()

        return ROLListResponse(
            rolList = (rolListFromES + rolListFromMSGraph)
                .toSortedSet(compareBy { it.navn }).toList()
        )
    }
}