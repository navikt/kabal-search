package no.nav.klage.search.api.mapper

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.search.api.view.*
import no.nav.klage.search.domain.*
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class KlagebehandlingerSearchCriteriaMapper(
    private val oAuthTokenService: OAuthTokenService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun kanBehandleEgenAnsatt() = oAuthTokenService.kanBehandleEgenAnsatt()
    fun kanBehandleFortrolig() = oAuthTokenService.kanBehandleFortrolig()
    fun kanBehandleStrengtFortrolig() = oAuthTokenService.kanBehandleStrengtFortrolig()

    fun toOppgaverOmPersonSearchCriteria(input: SearchPersonByFnrInput) = OppgaverOmPersonSearchCriteria(
        fnr = input.query,
        offset = 0,
        //TODO: Hvorfor setter vi denne? Hvorfor bare vise de to første??
        limit = 2,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    //-- saksbehandlers oppgaver:

    fun toSaksbehandlersFerdigstilteOppgaverSearchCriteria(
        navIdent: String,
        queryParams: MineFerdigstilteOppgaverQueryParams,
    ) = SaksbehandlersFerdigstilteOppgaverSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        saksbehandler = navIdent,
        ferdigstiltFom = mapFerdigstiltFom(queryParams),
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = queryParams.start,
        limit = queryParams.antall,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    fun toSaksbehandlersUferdigeOppgaverSearchCriteria(
        navIdent: String,
        queryParams: MineUferdigeOppgaverQueryParams,
    ) = SaksbehandlersUferdigeOppgaverSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        saksbehandler = navIdent,
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = queryParams.start,
        limit = queryParams.antall,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    //-- enhetens oppgaver:

    fun toEnhetensFerdigstilteOppgaverSearchCriteria(
        enhetId: String,
        queryParams: EnhetensFerdigstilteOppgaverQueryParams,
    ) = EnhetensFerdigstilteOppgaverSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        enhetId = enhetId,
        saksbehandlere = queryParams.tildelteSaksbehandlere,
        ferdigstiltFom = mapFerdigstiltFom(queryParams),
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = queryParams.start,
        limit = queryParams.antall,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    fun toEnhetensUferdigeOppgaverSearchCriteria(
        enhetId: String,
        queryParams: EnhetensUferdigeOppgaverQueryParams,
    ) = EnhetensUferdigeOppgaverSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        enhetId = enhetId,
        saksbehandlere = queryParams.tildelteSaksbehandlere,
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = queryParams.start,
        limit = queryParams.antall,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    //-- ledige oppgaver:

    fun toLedigeOppgaverSearchCriteria(queryParams: MineLedigeOppgaverQueryParams): LedigeOppgaverSearchCriteria =
        LedigeOppgaverSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            sortField = mapSortField(queryParams.sortering),
            order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
            offset = queryParams.start,
            limit = queryParams.antall,
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
            kanBehandleFortrolig = kanBehandleFortrolig(),
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        )

    fun toSearchCriteriaForLedigeMedUtgaattFrist(navIdent: String, queryParams: MineLedigeOppgaverQueryParams) =
        CountLedigeOppgaverMedUtgaattFristSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            fristFom = LocalDate.now().minusYears(15),
            fristTom = LocalDate.now().minusDays(1),
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
            kanBehandleFortrolig = kanBehandleFortrolig(),
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        )

    private fun mapSortField(sortering: Sortering?): SortField =
        when (sortering) {
            Sortering.MOTTATT -> SortField.MOTTATT
            Sortering.FRIST -> SortField.FRIST
            Sortering.ALDER -> SortField.MOTTATT
            else -> SortField.FRIST
        }

    private fun mapOrder(rekkefoelge: Rekkefoelge?, sortering: Sortering?): Order =
        if (rekkefoelge == Rekkefoelge.SYNKENDE) {
            if (sortering == Sortering.ALDER) {
                Order.ASC
            } else {
                Order.DESC
            }
        } else {
            if (sortering == Sortering.ALDER) {
                Order.DESC
            } else {
                Order.ASC
            }
        }

    //TODO: Skulle denne vært brukt??
    private fun mapFerdigstiltFom(queryParams: KlagebehandlingerQueryParams): LocalDate? {
        return if (queryParams.ferdigstiltDaysAgo != null) {
            LocalDate.now().minusDays(queryParams.ferdigstiltDaysAgo.toLong())
        } else {
            queryParams.ferdigstiltFom
        }
    }

    private fun mapFerdigstiltFom(queryParams: FerdigstilteOppgaverQueryParams): LocalDate =
        LocalDate.now().minusDays(queryParams.ferdigstiltDaysAgo)

}


