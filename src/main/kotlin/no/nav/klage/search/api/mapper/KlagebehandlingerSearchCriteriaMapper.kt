package no.nav.klage.search.api.mapper

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.search.api.view.KlagebehandlingerQueryParams
import no.nav.klage.search.api.view.SearchPersonByFnrInput
import no.nav.klage.search.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class KlagebehandlingerSearchCriteriaMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun toSearchCriteria(input: SearchPersonByFnrInput) = KlagebehandlingerSearchCriteria(
        foedselsnr = input.query,
        statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.ALLE,
        offset = 0,
        limit = 2
    )

    fun toSearchCriteria(
        navIdent: String,
        queryParams: KlagebehandlingerQueryParams,
        enhet: Enhet
    ) = KlagebehandlingerSearchCriteria(
        enhetId = if (queryParams.erTildeltSaksbehandler == true && queryParams.tildeltSaksbehandler.isEmpty()) enhet.enhetId else null,
        typer = queryParams.typer.map { Type.of(it) },
        ytelser = queryParams.ytelser.map { Ytelse.of(it) },
        hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
        order = if (queryParams.rekkefoelge == KlagebehandlingerQueryParams.Rekkefoelge.SYNKENDE) {
            KlagebehandlingerSearchCriteria.Order.DESC
        } else {
            KlagebehandlingerSearchCriteria.Order.ASC
        },
        offset = queryParams.start,
        limit = queryParams.antall,
        erTildeltSaksbehandler = queryParams.erTildeltSaksbehandler,
        saksbehandlere = queryParams.tildeltSaksbehandler,
        projection = queryParams.projeksjon?.let { KlagebehandlingerSearchCriteria.Projection.valueOf(it.name) },
        sortField = if (queryParams.sortering == KlagebehandlingerQueryParams.Sortering.MOTTATT) {
            KlagebehandlingerSearchCriteria.SortField.MOTTATT
        } else {
            KlagebehandlingerSearchCriteria.SortField.FRIST
        },
        ferdigstiltFom = getFerdigstiltFom(queryParams),
        statuskategori = if (queryParams.ferdigstiltFom != null) {
            KlagebehandlingerSearchCriteria.Statuskategori.AVSLUTTET
        } else {
            KlagebehandlingerSearchCriteria.Statuskategori.AAPEN
        }
    )

    private fun getFerdigstiltFom(queryParams: KlagebehandlingerQueryParams): LocalDate? {
        return if (queryParams.ferdigstiltDaysAgo != null) {
            LocalDate.now().minusDays(queryParams.ferdigstiltDaysAgo.toLong())
        } else {
            queryParams.ferdigstiltFom
        }
    }

    fun toFristUtgaattIkkeTildeltSearchCriteria(navIdent: String, queryParams: KlagebehandlingerQueryParams) =
        KlagebehandlingerSearchCriteria(
            typer = queryParams.typer.map { Type.of(it) },
            hjemler = queryParams.hjemler.map { Hjemmel.of(it) },
            offset = 0,
            limit = 1,
            erTildeltSaksbehandler = false,
            fristFom = LocalDate.now().minusYears(15),
            fristTom = LocalDate.now().minusDays(1),
        )
}


