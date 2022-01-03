package no.nav.klage.search.api.mapper

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.search.api.view.*
import no.nav.klage.search.domain.KlagebehandlingerSearchCriteria
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
        statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.ALLE,
        offset = 0,
        limit = 2,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    fun toSearchCriteria(
        navIdent: String,
        queryParams: MineFerdigstilteOppgaverQueryParams,
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
        ferdigstiltFom = mapFerdigstiltFom(queryParams),
        statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.AVSLUTTET,
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
        statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.AAPEN,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    fun toSearchCriteria(
        queryParams: MineLedigeOppgaverQueryParams,
    ) = KlagebehandlingerSearchCriteria(
        enhetId = null,
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        order = mapOrder(queryParams.rekkefoelge, queryParams.sortering),
        offset = queryParams.start,
        limit = queryParams.antall,
        erTildeltSaksbehandler = false,
        saksbehandlere = emptyList(),
        sortField = mapSortField(queryParams.sortering),
        ferdigstiltFom = null,
        statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.AAPEN,
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
        statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.AAPEN,
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
        statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.AVSLUTTET,
        kanBehandleEgenAnsatt = kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig(),
    )

    private fun mapSortField(sortering: Sortering?): KlagebehandlingerSearchCriteria.SortField =
        when (sortering) {
            Sortering.MOTTATT -> KlagebehandlingerSearchCriteria.SortField.MOTTATT
            Sortering.FRIST -> KlagebehandlingerSearchCriteria.SortField.FRIST
            Sortering.ALDER -> KlagebehandlingerSearchCriteria.SortField.MOTTATT
            else -> KlagebehandlingerSearchCriteria.SortField.FRIST
        }

    private fun mapOrder(rekkefoelge: Rekkefoelge?, sortering: Sortering?): KlagebehandlingerSearchCriteria.Order =
        if (rekkefoelge == Rekkefoelge.SYNKENDE) {
            if (sortering == Sortering.ALDER) {
                KlagebehandlingerSearchCriteria.Order.ASC
            } else {
                KlagebehandlingerSearchCriteria.Order.DESC
            }
        } else {
            if (sortering == Sortering.ALDER) {
                KlagebehandlingerSearchCriteria.Order.DESC
            } else {
                KlagebehandlingerSearchCriteria.Order.ASC
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
            KlagebehandlingerSearchCriteria.Order.DESC
        } else {
            KlagebehandlingerSearchCriteria.Order.ASC
        },
        offset = queryParams.start,
        limit = queryParams.antall,
        erTildeltSaksbehandler = queryParams.erTildeltSaksbehandler,
        saksbehandlere = queryParams.tildeltSaksbehandler,
        //projection = queryParams.projeksjon?.let { KlagebehandlingerSearchCriteria.Projection.valueOf(it.name) },
        sortField = if (queryParams.sortering == Sortering.MOTTATT) {
            KlagebehandlingerSearchCriteria.SortField.MOTTATT
        } else {
            KlagebehandlingerSearchCriteria.SortField.FRIST
        },
        ferdigstiltFom = mapFerdigstiltFom(queryParams),
        statuskategori = if (queryParams.ferdigstiltFom != null || queryParams.ferdigstiltDaysAgo != null) {
            KlagebehandlingerSearchCriteria.Statuskategori.AVSLUTTET
        } else {
            KlagebehandlingerSearchCriteria.Statuskategori.AAPEN
        },
        kanBehandleEgenAnsatt = innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt(),
        kanBehandleFortrolig = innloggetSaksbehandlerRepository.kanBehandleFortrolig(),
        kanBehandleStrengtFortrolig = innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig(),
    )

    private fun mapFerdigstiltFom(queryParams: KlagebehandlingerQueryParams): LocalDate? {
        return if (queryParams.ferdigstiltDaysAgo != null) {
            LocalDate.now().minusDays(queryParams.ferdigstiltDaysAgo!!.toLong())
        } else {
            queryParams.ferdigstiltFom
        }
    }

    private fun mapFerdigstiltFom(queryParams: FerdigstilteOppgaverQueryParams): LocalDate? =
        LocalDate.now().minusDays(queryParams.ferdigstiltDaysAgo)


    fun toFristUtgaattIkkeTildeltSearchCriteria(navIdent: String, queryParams: KlagebehandlingerQueryParams) =
        KlagebehandlingerSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            offset = 0,
            limit = 1,
            erTildeltSaksbehandler = false,
            fristFom = LocalDate.now().minusYears(15),
            fristTom = LocalDate.now().minusDays(1),
            kanBehandleEgenAnsatt = innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt(),
            kanBehandleFortrolig = innloggetSaksbehandlerRepository.kanBehandleFortrolig(),
            kanBehandleStrengtFortrolig = innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig(),
        )
}


