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
class BehandlingerSearchCriteriaMapper(
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
        limit = 500,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        sortField = SortField.MOTTATT,
        order = Order.DESC,
    )

    //-- saksbehandlers oppgaver:

    fun toFerdigstilteOppgaverSearchCriteria(
        navIdent: String,
        queryParams: MineFerdigstilteOppgaverQueryParams,
    ) = FerdigstilteOppgaverSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        navIdent = navIdent,
        ferdigstiltFom = mapFrom(queryParams.ferdigstiltFrom),
        ferdigstiltTom = queryParams.ferdigstiltTo ?: LocalDate.now(),
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = 0,
        limit = 9_999,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        fristFrom = mapFrom(queryParams.fristFrom),
        fristTo = mapFristTo(queryParams.fristTo),
    )

    fun toReturnerteROLOppgaverSearchCriteria(
        navIdent: String,
        queryParams: MineReturnerteROLOppgaverQueryParams,
    ) = ReturnerteROLOppgaverSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        navIdent = navIdent,
        returnertFom = mapFrom(queryParams.returnertFrom),
        returnertTom = queryParams.returnertTo ?: LocalDate.now(),
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = 0,
        limit = 9_999,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        fristFrom = mapFrom(queryParams.fristFrom),
        fristTo = mapFristTo(queryParams.fristTo),
    )

    fun toUferdigeOppgaverSearchCriteria(
        navIdent: String,
        queryParams: MineUferdigeOppgaverQueryParams,
    ) = UferdigeOppgaverSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        navIdent = navIdent,
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = 0,
        limit = 9_999,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        fristFrom = mapFrom(queryParams.fristFrom),
        fristTo = mapFristTo(queryParams.fristTo),
    )

    fun toOppgaverPaaVentSearchCriteria(
        navIdent: String,
        queryParams: MineOppgaverPaaVentQueryParams,
    ) = OppgaverPaaVentSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        navIdent = navIdent,
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = 0,
        limit = 9_999,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        fristFrom = mapFrom(queryParams.fristFrom),
        fristTo = mapFristTo(queryParams.fristTo),
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
        ferdigstiltFom = mapFrom(queryParams.ferdigstiltFrom),
        ferdigstiltTom = queryParams.ferdigstiltTo ?: LocalDate.now(),
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = 0,
        limit = 9_999,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        fristFrom = mapFrom(queryParams.fristFrom),
        fristTo = mapFristTo(queryParams.fristTo),
    )

    fun toEnhetensOppgaverPaaVentSearchCriteria(
        enhetId: String,
        queryParams: EnhetensOppgaverPaaVentQueryParams,
    ) = EnhetensOppgaverPaaVentSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        enhetId = enhetId,
        saksbehandlere = queryParams.tildelteSaksbehandlere,
        medunderskrivere = queryParams.medunderskrivere,
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = 0,
        limit = 9_999,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        fristFrom = mapFrom(queryParams.fristFrom),
        fristTo = mapFristTo(queryParams.fristTo),
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
        medunderskrivere = queryParams.medunderskrivere,
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = 0,
        limit = 9_999,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        fristFrom = mapFrom(queryParams.fristFrom),
        fristTo = mapFristTo(queryParams.fristTo),
    )

    fun toKrolsUferdigeOppgaverSearchCriteria(
        queryParams: KrolsUferdigeOppgaverQueryParams,
    ) = KrolsUferdigeOppgaverSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        rolList = queryParams.tildelteRol,
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = 0,
        limit = 9_999,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        fristFrom = mapFrom(queryParams.fristFrom),
        fristTo = mapFristTo(queryParams.fristTo),
    )

    fun toKrolsReturnerteOppgaverSearchCriteria(
        queryParams: KrolsReturnerteOppgaverQueryParams,
    ) = KrolsReturnerteOppgaverSearchCriteria(
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        returnertFom = mapFrom(queryParams.returnertFrom),
        returnertTom = queryParams.returnertTo ?: LocalDate.now(),
        sortField = mapSortField(queryParams.sortering),
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = 0,
        limit = 9_999,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        fristFrom = mapFrom(queryParams.fristFrom),
        fristTo = mapFristTo(queryParams.fristTo),
    )

    //-- ledige oppgaver:

    fun toLedigeOppgaverSearchCriteria(queryParams: MineLedigeOppgaverQueryParams): LedigeOppgaverSearchCriteria =
        LedigeOppgaverSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            sortField = mapSortField(queryParams.sortering),
            order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
            offset = 0,
            limit = 9_999,
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
            kanBehandleFortrolig = kanBehandleFortrolig(),
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
        )

    fun toSearchCriteriaForLedigeMedUtgaattFrist(queryParams: MineLedigeOppgaverCountQueryParams) =
        CountLedigeOppgaverMedUtgaattFristSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            fristFrom = LocalDate.now().minusYears(15),
            fristTo = LocalDate.now().minusDays(1),
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
            kanBehandleFortrolig = kanBehandleFortrolig(),
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        )

    fun toBehandlingIdSearchCriteria(behandlingId: String): BehandlingIdSearchCriteria =
        BehandlingIdSearchCriteria(
            behandlingId = behandlingId,
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
            kanBehandleFortrolig = kanBehandleFortrolig(),
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        )

    private fun mapSortField(sortering: Sortering?): SortField =
        when (sortering) {
            Sortering.MOTTATT -> SortField.MOTTATT
            Sortering.FRIST -> SortField.FRIST
            Sortering.ALDER -> SortField.MOTTATT
            Sortering.PAA_VENT_FROM -> SortField.PAA_VENT_FROM
            Sortering.PAA_VENT_TO -> SortField.PAA_VENT_TO
            Sortering.AVSLUTTET_AV_SAKSBEHANDLER -> SortField.AVSLUTTET_AV_SAKSBEHANDLER
            Sortering.RETURNERT_FRA_ROL -> SortField.RETURNERT_FRA_ROL
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

    private fun mapFrom(from: LocalDate?): LocalDate {
        return from ?: LocalDate.now().minusDays(36500)
    }

    private fun mapFristTo(fristTo: LocalDate?): LocalDate {
        return fristTo ?: LocalDate.now().plusDays(36500)
    }
}
