package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.search.api.view.MedunderskrivereListResponse
import no.nav.klage.search.api.view.ROLListResponse
import no.nav.klage.search.api.view.SaksbehandlerView
import no.nav.klage.search.api.view.SaksbehandlereListResponse
import no.nav.klage.search.clients.klagelookup.KlageLookupClient
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.ROLListSearchCriteria
import no.nav.klage.search.domain.SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.saksbehandler.InnloggetSaksbehandlerService
import no.nav.klage.search.service.saksbehandler.SaksbehandlerService
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "kabal-search")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class SaksbehandlerController(
    private val elasticsearchService: ElasticsearchService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val saksbehandlerService: SaksbehandlerService,
    private val klageLookupClient: KlageLookupClient,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
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

        val navIdent = tokenUtil.getIdent()
        if (innloggetSaksbehandlerService.getEnhetForSaksbehandler().enhetId != enhet) {
            throw MissingTilgangException("Saksbehandler $navIdent does not have access to enhet $enhet")
        }

        val userGroups = klageLookupClient.getUserGroups(navIdent).groups

        val esResponse = elasticsearchService.findSaksbehandlereByEnhetCriteria(
            SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria(
                enhet = enhet,
                kanBehandleEgenAnsatt = userGroups.contains(AzureGroup.EGEN_ANSATT),
                kanBehandleFortrolig = userGroups.contains(AzureGroup.FORTROLIG),
                kanBehandleStrengtFortrolig = userGroups.contains(AzureGroup.STRENGT_FORTROLIG),
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

        val navIdent = tokenUtil.getIdent()
        if (innloggetSaksbehandlerService.getEnhetForSaksbehandler().enhetId != enhet) {
            throw MissingTilgangException("Saksbehandler $navIdent does not have access to enhet $enhet")
        }

        val userGroups = klageLookupClient.getUserGroups(navIdent).groups

        val esResponse = elasticsearchService.findMedunderskrivereByEnhetCriteria(
            SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria(
                enhet = enhet,
                kanBehandleEgenAnsatt = userGroups.contains(AzureGroup.EGEN_ANSATT),
                kanBehandleFortrolig = userGroups.contains(AzureGroup.FORTROLIG),
                kanBehandleStrengtFortrolig = userGroups.contains(AzureGroup.STRENGT_FORTROLIG),
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

        val navIdent = tokenUtil.getIdent()
        val userGroups = klageLookupClient.getUserGroups(navIdent).groups

        val esResponse = elasticsearchService.findROLListByEnhetCriteria(
            ROLListSearchCriteria(
                kanBehandleEgenAnsatt = userGroups.contains(AzureGroup.EGEN_ANSATT),
                kanBehandleFortrolig = userGroups.contains(AzureGroup.FORTROLIG),
                kanBehandleStrengtFortrolig = userGroups.contains(AzureGroup.STRENGT_FORTROLIG),
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