package no.nav.klage.search.api.mapper

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.search.api.view.*
import no.nav.klage.search.domain.*
import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class KlagebehandlingerSearchCriteriaMapper(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun kanBehandleEgenAnsatt() = innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt()
    fun kanBehandleFortrolig() = innloggetSaksbehandlerRepository.kanBehandleFortrolig()
    fun kanBehandleStrengtFortrolig() = innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig()

    fun toSearchCriteria(input: SearchPersonByFnrInput) = KlagebehandlingerSearchCriteria(
        foedselsnr = input.query,
        statuskategori = Statuskategori.ALLE,
        offset = 0,
        limit = 2,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

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

    fun toSearchCriteria(
        navIdent: String,
        queryParams: MineUferdigeOppgaverQueryParams,
    ) = KlagebehandlingerSearchCriteria(
        enhetId = null,
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = queryParams.start,
        limit = queryParams.antall,
        erTildeltSaksbehandler = true,
        saksbehandlere = listOf(navIdent),
        sortField = mapSortField(queryParams.sortering),
        ferdigstiltFom = null,
        statuskategori = Statuskategori.AAPEN,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    fun toLedigeOppgaverSearchCriteria(queryParams: MineLedigeOppgaverQueryParams): LedigeOppgaverSearchCriteria =
        LedigeOppgaverSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
            offset = queryParams.start,
            limit = queryParams.antall,
            sortField = mapSortField(queryParams.sortering),
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
            kanBehandleFortrolig = kanBehandleFortrolig(),
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        )

    fun toSearchCriteria(
        enhetId: String,
        queryParams: EnhetensUferdigeOppgaverQueryParams,
    ) = KlagebehandlingerSearchCriteria(
        enhetId = enhetId,
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = queryParams.start,
        limit = queryParams.antall,
        erTildeltSaksbehandler = true,
        saksbehandlere = queryParams.tildelteSaksbehandlere,
        sortField = mapSortField(queryParams.sortering),
        ferdigstiltFom = null,
        statuskategori = Statuskategori.AAPEN,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    fun toSearchCriteria(
        enhetId: String,
        queryParams: EnhetensFerdigstilteOppgaverQueryParams,
    ) = KlagebehandlingerSearchCriteria(
        enhetId = enhetId,
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = queryParams.start,
        limit = queryParams.antall,
        erTildeltSaksbehandler = true,
        saksbehandlere = queryParams.tildelteSaksbehandlere,
        sortField = mapSortField(queryParams.sortering),
        ferdigstiltFom = mapFerdigstiltFom(queryParams),
        statuskategori = Statuskategori.AVSLUTTET,
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

    fun toSearchCriteria(
        navIdent: String,
        queryParams: KlagebehandlingerQueryParams,
        enhet: Enhet
    ) = KlagebehandlingerSearchCriteria(
        enhetId = if (queryParams.erTildeltSaksbehandler == true && queryParams.tildeltSaksbehandler.isEmpty()) enhet.enhetId else null,
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        order = if (queryParams.rekkefoelge == Rekkefoelge.SYNKENDE) {
            Order.DESC
        } else {
            Order.ASC
        },
        offset = queryParams.start,
        limit = queryParams.antall,
        erTildeltSaksbehandler = queryParams.erTildeltSaksbehandler,
        saksbehandlere = queryParams.tildeltSaksbehandler,
        //projection = queryParams.projeksjon?.let { KlagebehandlingerSearchCriteria.Projection.valueOf(it.name) },
        sortField = if (queryParams.sortering == Sortering.MOTTATT) {
            SortField.MOTTATT
        } else {
            SortField.FRIST
        },
        ferdigstiltFom = mapFerdigstiltFom(queryParams),
        statuskategori = if (queryParams.ferdigstiltFom != null || queryParams.ferdigstiltDaysAgo != null) {
            Statuskategori.AVSLUTTET
        } else {
            Statuskategori.AAPEN
        },
        kanBehandleEgenAnsatt = innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = innloggetSaksbehandlerRepository.kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig(),
    )

    private fun mapFerdigstiltFom(queryParams: KlagebehandlingerQueryParams): LocalDate? {
        return if (queryParams.ferdigstiltDaysAgo != null) {
            LocalDate.now().minusDays(queryParams.ferdigstiltDaysAgo.toLong())
        } else {
            queryParams.ferdigstiltFom
        }
    }

    private fun mapFerdigstiltFom(queryParams: FerdigstilteOppgaverQueryParams): LocalDate =
        LocalDate.now().minusDays(queryParams.ferdigstiltDaysAgo)


    fun toSearchCriteriaForLedigeMedUtgaattFrist(navIdent: String, queryParams: MineLedigeOppgaverQueryParams) =
        KlagebehandlingerSearchCriteria(
            enhetId = null,
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            erTildeltSaksbehandler = false,
            saksbehandlere = emptyList(),
            ferdigstiltFom = null,
            statuskategori = Statuskategori.AAPEN,
            fristFom = LocalDate.now().minusYears(15),
            fristTom = LocalDate.now().minusDays(1),
            offset = 0,
            limit = 1,
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
            kanBehandleFortrolig = kanBehandleFortrolig(),
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        )

    fun toFristUtgaattIkkeTildeltSearchCriteria(navIdent: String, queryParams: KlagebehandlingerQueryParams) =
        KlagebehandlingerSearchCriteria(
            enhetId = null,
            typer = queryParams.typer.map { Type.of(it) },
            ytelser = queryParams.ytelser.map { Ytelse.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            erTildeltSaksbehandler = false,
            saksbehandlere = emptyList(),
            ferdigstiltFom = null,
            statuskategori = Statuskategori.AAPEN,
            fristFom = LocalDate.now().minusYears(15),
            fristTom = LocalDate.now().minusDays(1),
            offset = 0,
            limit = 1,
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
            kanBehandleFortrolig = kanBehandleFortrolig(),
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
        )

}


