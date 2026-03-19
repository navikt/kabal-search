package no.nav.klage.search.api.mapper

import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.kodeverk.SattPaaVentReason
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.search.api.view.*
import no.nav.klage.search.clients.klagelookup.KlageLookupClient
import no.nav.klage.search.domain.*
import no.nav.klage.search.util.TokenUtil
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BehandlingerSearchCriteriaMapper(
    private val klageLookupClient: KlageLookupClient,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    private data class BehandlingPermissions(
        val kanBehandleEgenAnsatt: Boolean,
        val kanBehandleFortrolig: Boolean,
        val kanBehandleStrengtFortrolig: Boolean,
    )

    private fun resolvePermissions(navIdent: String = tokenUtil.getIdent()): BehandlingPermissions {
        val userGroups = klageLookupClient.getUserGroups(navIdent).groups
        return BehandlingPermissions(
            kanBehandleEgenAnsatt = userGroups.contains(AzureGroup.EGEN_ANSATT),
            kanBehandleFortrolig = userGroups.contains(AzureGroup.FORTROLIG),
            kanBehandleStrengtFortrolig = userGroups.contains(AzureGroup.STRENGT_FORTROLIG),
        )
    }

    fun toOppgaverOmPersonSearchCriteria(input: SearchPersonByFnrInput): OppgaverOmPersonSearchCriteria {
        val permissions = resolvePermissions()
        return OppgaverOmPersonSearchCriteria(
            fnr = input.query,
            offset = 0,
            limit = 500,
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            sortField = SortField.MOTTATT,
            order = Order.DESC,
        )
    }

    //-- saksbehandlers oppgaver:

    fun toSaksbehandlersFerdigstilteOppgaverSearchCriteria(
        navIdent: String,
        queryParams: SaksbehandlersFerdigstilteOppgaverQueryParams,
    ): SaksbehandlersFerdigstilteOppgaverSearchCriteria {
        val permissions = resolvePermissions()
        return SaksbehandlersFerdigstilteOppgaverSearchCriteria(
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
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
        )
    }

    fun toReturnerteROLOppgaverSearchCriteria(
        navIdent: String,
        queryParams: MineReturnerteROLOppgaverQueryParams,
    ): ReturnerteROLOppgaverSearchCriteria {
        val permissions = resolvePermissions()
        return ReturnerteROLOppgaverSearchCriteria(
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
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
        )
    }

    fun toSaksbehandlersUferdigeOppgaverSearchCriteria(
        navIdent: String,
        queryParams: SaksbehandlersUferdigeOppgaverQueryParams,
    ): SaksbehandlersUferdigeOppgaverSearchCriteria {
        val permissions = resolvePermissions()
        return SaksbehandlersUferdigeOppgaverSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            navIdent = navIdent,
            sortField = mapSortField(queryParams.sortering),
            order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
            offset = 0,
            limit = 9_999,
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
            helperStatusList = queryParams.helperStatusList,
        )
    }

    fun toSaksbehandlersOppgaverPaaVentSearchCriteria(
        navIdent: String,
        queryParams: SaksbehandlersOppgaverPaaVentQueryParams,
    ): SaksbehandlersOppgaverPaaVentSearchCriteria {
        val permissions = resolvePermissions()
        return SaksbehandlersOppgaverPaaVentSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            navIdent = navIdent,
            sattPaaVentReasons = queryParams.sattPaaVentReasonIds.map { SattPaaVentReason.of(it) },
            sortField = mapSortField(queryParams.sortering),
            order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
            offset = 0,
            limit = 9_999,
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
        )
    }

    //-- enhetens oppgaver:

    fun toEnhetensFerdigstilteOppgaverSearchCriteria(
        enhetId: String,
        queryParams: EnhetensFerdigstilteOppgaverQueryParams,
    ): EnhetensFerdigstilteOppgaverSearchCriteria {
        val permissions = resolvePermissions()
        return EnhetensFerdigstilteOppgaverSearchCriteria(
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
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
        )
    }

    fun toEnhetensOppgaverPaaVentSearchCriteria(
        enhetId: String,
        queryParams: EnhetensOppgaverPaaVentQueryParams,
    ): EnhetensOppgaverPaaVentSearchCriteria {
        val permissions = resolvePermissions()
        return EnhetensOppgaverPaaVentSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            enhetId = enhetId,
            saksbehandlere = queryParams.tildelteSaksbehandlere,
            medunderskrivere = queryParams.medunderskrivere,
            sattPaaVentReasons = queryParams.sattPaaVentReasonIds.map { SattPaaVentReason.of(it) },
            sortField = mapSortField(queryParams.sortering),
            order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
            offset = 0,
            limit = 9_999,
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
        )
    }

    fun toEnhetensUferdigeOppgaverSearchCriteria(
        enhetId: String,
        queryParams: EnhetensUferdigeOppgaverQueryParams,
    ): EnhetensUferdigeOppgaverSearchCriteria {
        val permissions = resolvePermissions()
        return EnhetensUferdigeOppgaverSearchCriteria(
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
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
            helperStatusList = queryParams.helperStatusList,
        )
    }

    fun toKrolsUferdigeOppgaverSearchCriteria(
        queryParams: KrolsUferdigeOppgaverQueryParams,
    ): KrolsUferdigeOppgaverSearchCriteria {
        val permissions = resolvePermissions()
        return KrolsUferdigeOppgaverSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            rolList = queryParams.tildelteRol,
            sortField = mapSortField(queryParams.sortering),
            order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
            offset = 0,
            limit = 9_999,
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
        )
    }

    fun toKrolsReturnerteOppgaverSearchCriteria(
        queryParams: KrolsReturnerteOppgaverQueryParams,
    ): KrolsReturnerteOppgaverSearchCriteria {
        val permissions = resolvePermissions()
        return KrolsReturnerteOppgaverSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            returnertFom = mapFrom(queryParams.returnertFrom),
            returnertTom = queryParams.returnertTo ?: LocalDate.now(),
            sortField = mapSortField(queryParams.sortering),
            order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
            offset = 0,
            limit = 9_999,
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
        )
    }

    //-- ledige oppgaver:

    fun toLedigeOppgaverSearchCriteria(queryParams: SaksbehandlersLedigeOppgaverQueryParams): LedigeOppgaverSearchCriteria {
        val permissions = resolvePermissions()
        return LedigeOppgaverSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            sortField = mapSortField(queryParams.sortering),
            order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
            offset = 0,
            limit = 9_999,
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
            fristFrom = mapFrom(queryParams.fristFrom),
            fristTo = mapFristTo(queryParams.fristTo),
            varsletFristFrom = mapFrom(queryParams.varsletFristFrom),
            varsletFristTo = mapFristTo(queryParams.varsletFristTo),
        )
    }

    fun toSearchCriteriaForLedigeMedUtgaattFrist(queryParams: SaksbehandlersLedigeOppgaverCountQueryParams): CountLedigeOppgaverMedUtgaattFristSearchCriteria {
        val permissions = resolvePermissions()
        return CountLedigeOppgaverMedUtgaattFristSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            fristFrom = LocalDate.now().minusYears(15),
            fristTo = LocalDate.now().minusDays(1),
            varsletFristFrom = LocalDate.now().minusYears(15),
            varsletFristTo = LocalDate.now().plusYears(15),
            kanBehandleEgenAnsatt = permissions.kanBehandleEgenAnsatt,
            kanBehandleFortrolig = permissions.kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = permissions.kanBehandleStrengtFortrolig,
        )
    }

    private fun mapSortField(sortering: Sortering?): SortField =
        when (sortering) {
            Sortering.MOTTATT -> SortField.MOTTATT
            Sortering.FRIST -> SortField.FRIST
            Sortering.ALDER -> SortField.MOTTATT
            Sortering.PAA_VENT_FROM -> SortField.PAA_VENT_FROM
            Sortering.PAA_VENT_TO -> SortField.PAA_VENT_TO
            Sortering.AVSLUTTET_AV_SAKSBEHANDLER -> SortField.AVSLUTTET_AV_SAKSBEHANDLER
            Sortering.RETURNERT_FRA_ROL -> SortField.RETURNERT_FRA_ROL
            Sortering.VARSLET_FRIST -> SortField.VARSLET_FRIST
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
