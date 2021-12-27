package no.nav.klage.search.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.api.mapper.KlagebehandlingListMapper
import no.nav.klage.search.api.mapper.KlagebehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.*
import no.nav.klage.search.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.search.domain.kodeverk.ytelseTilHjemler
import no.nav.klage.search.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.exceptions.NotMatchingUserException
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.service.ElasticsearchService
import no.nav.klage.search.service.SaksbehandlerService
import no.nav.klage.search.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["kabal-search"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/enhet/{enhetId}")
class OppgaverListController(
    private val klagebehandlingListMapper: KlagebehandlingListMapper,
    private val elasticsearchService: ElasticsearchService,
    private val klagebehandlingerSearchCriteriaMapper: KlagebehandlingerSearchCriteriaMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerService: SaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent ferdigstilte oppgaver for en ansatt",
        notes = "Henter alle ferdigstilte oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver/ferdigstilte", produces = ["application/json"])
    fun getMineFerdigstilteOppgaver(
        @ApiParam(value = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: MineFerdigstilteOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser =
            lovligeValgteYtelser(queryParams, saksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter)
        val hjemler: List<String> = lovligeValgteHjemler(queryParams, ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSearchCriteria(
            navIdent,
            queryParams.copy(ytelser = ytelser, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esResponse.searchHits.map { it.content },
                searchCriteria.isProjectionUtvidet(),
                searchCriteria.ferdigstiltFom != null,
                searchCriteria.saksbehandlere,
                getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }

    @ApiOperation(
        value = "Hent uferdige oppgaver for en ansatt",
        notes = "Henter alle uferdige oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver/uferdige", produces = ["application/json"])
    fun getMineUferdigeOppgaver(
        @ApiParam(value = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: MineUferdigeOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser =
            lovligeValgteYtelser(queryParams, saksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter)
        val hjemler: List<String> = lovligeValgteHjemler(queryParams, ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSearchCriteria(
            navIdent,
            queryParams.copy(ytelser = ytelser, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esResponse.searchHits.map { it.content },
                searchCriteria.isProjectionUtvidet(),
                searchCriteria.ferdigstiltFom != null,
                searchCriteria.saksbehandlere,
                getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }

    @ApiOperation(
        value = "Hent enhetens ferdigstilte oppgaver",
        notes = "Henter alle ferdigstilte oppgaver for enheten som saksbehandler har tilgang til."
    )
    @GetMapping("/oppgaver/tildelte/ferdigstilte", produces = ["application/json"])
    fun getEnhetensFerdigstilteOppgaver(
        @ApiParam(value = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensFerdigstilteOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser = lovligeValgteYtelser(queryParams, listOf(valgtEnhet))
        val hjemler: List<String> = lovligeValgteHjemler(queryParams, ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSearchCriteria(
            enhetId,
            queryParams.copy(ytelser = ytelser, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esResponse.searchHits.map { it.content },
                searchCriteria.isProjectionUtvidet(),
                searchCriteria.ferdigstiltFom != null,
                searchCriteria.saksbehandlere,
                getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }

    @ApiOperation(
        value = "Hent uferdige oppgaver for en enhet",
        notes = "Henter alle uferdige oppgaver i enheten som saksbehandler har tilgang til."
    )
    @GetMapping("/oppgaver/tildelte/uferdige", produces = ["application/json"])
    fun getEnhetensUferdigeOppgaver(
        @ApiParam(value = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensUferdigeOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateRettigheterForEnhetensTildelteOppgaver()

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser = lovligeValgteYtelser(queryParams, listOf(valgtEnhet))
        val hjemler: List<String> = lovligeValgteHjemler(queryParams, ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSearchCriteria(
            enhetId,
            queryParams.copy(ytelser = ytelser, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esResponse.searchHits.map { it.content },
                searchCriteria.isProjectionUtvidet(),
                searchCriteria.ferdigstiltFom != null,
                searchCriteria.saksbehandlere,
                getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }

    @ApiOperation(
        value = "Hent ledige oppgaver for en enhet",
        notes = "Henter alle ledige oppgaver i enheten som saksbehandler har tilgang til."
    )
    @GetMapping("/oppgaver/tildelte/uferdige", produces = ["application/json"])
    fun getEnhetensLedigeOppgaver(
        @ApiParam(value = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        queryParams: EnhetensLedigeOppgaverQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        val ytelser = lovligeValgteYtelser(queryParams, listOf(valgtEnhet))
        val hjemler: List<String> = lovligeValgteHjemler(queryParams, ytelser)
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSearchCriteria(
            enhetId,
            queryParams.copy(ytelser = ytelser, hjemler = hjemler),
        )

        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esResponse.searchHits.map { it.content },
                searchCriteria.isProjectionUtvidet(),
                searchCriteria.ferdigstiltFom != null,
                searchCriteria.saksbehandlere,
                getAlleYtelserInnloggetSaksbehandlerKanBehandle()
            )
        )
    }

    @ApiOperation(
        value = "Hent antall utildelte klagebehandlinger for enheten der fristen gått ut",
        notes = "Teller opp alle utildelte klagebehandlinger for enheten der fristen gått ut."
    )
    @GetMapping("/ansatte/{navIdent}/antallklagebehandlingermedutgaattefrister", produces = ["application/json"])
    fun getAntallUtgaatteFrister(
        @ApiParam(value = "EnhetId til enheten den ansatte jobber i")
        @PathVariable enhetId: String,
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: KlagebehandlingerQueryParams
    ): AntallUtgaatteFristerResponse {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        val valgtEnhet = getEnhetOrThrowException(enhetId)
        //TODO: Denne matcher ikke sånn vi gjør når vi henter ut oppgavene, bør vel samkjøres
        val ytelser = lovligeValgteYtelser(queryParams, listOf(valgtEnhet))
        val hjemler: List<String> = lovligeValgteHjemler(queryParams, ytelser)
        return AntallUtgaatteFristerResponse(
            antall = elasticsearchService.countByCriteria(
                klagebehandlingerSearchCriteriaMapper.toFristUtgaattIkkeTildeltSearchCriteria(
                    navIdent,
                    queryParams.copy(ytelser = ytelser, hjemler = hjemler),
                )
            )
        )
    }

    private fun validateNavIdent(navIdent: String) {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }
    }

    private fun getEnhetOrThrowException(enhetId: String): EnhetMedLovligeYtelser =
        saksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter.find { it.enhet.enhetId == enhetId }
            ?: throw IllegalArgumentException("Saksbehandler har ikke tilgang til angitt enhet")

    private fun validateRettigheterForEnhetensTildelteOppgaver() {
        if (!(innloggetSaksbehandlerRepository.isLeder() || innloggetSaksbehandlerRepository.isFagansvarlig())) {
            val message =
                "${innloggetSaksbehandlerRepository.getInnloggetIdent()} har ikke tilgang til å se alle tildelte oppgaver."
            logger.warn(message)
            throw MissingTilgangException(message)
        }
    }

    private fun lovligeValgteYtelser(
        queryParams: CommonOppgaverQueryParams,
        valgteEnheter: List<EnhetMedLovligeYtelser>
    ) =
        if (queryParams.ytelser.isEmpty()) {
            valgteEnheter.flatMap { it.ytelser }.map { it.id }
        } else {
            valgteEnheter.flatMap { it.ytelser }.map { it.id }.intersect(queryParams.ytelser)
        }.toList()

    private fun lovligeValgteHjemler(queryParams: CommonOppgaverQueryParams, ytelser: List<String>) =
        if (queryParams.hjemler.isEmpty()) {
            ytelser.mapNotNull { ytelseTilHjemler.get(Ytelse.of(it)) }.flatten().map { it.id }
        } else {
            ytelser.mapNotNull { ytelseTilHjemler.get(Ytelse.of(it)) }.flatten().map { it.id }
                .intersect(queryParams.hjemler).toList()
        }

    private fun getAlleYtelserInnloggetSaksbehandlerKanBehandle() =
        saksbehandlerService.getEnheterMedYtelserForSaksbehandler().enheter.flatMap { it.ytelser }
}

