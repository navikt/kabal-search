package no.nav.klage.search.api.mapper

import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.kodeverk.MedunderskriverFlyt
import no.nav.klage.search.domain.kodeverk.Tema
import no.nav.klage.search.domain.kodeverk.Type
import no.nav.klage.search.domain.kodeverk.Ytelse
import java.time.LocalDate
import java.time.LocalDateTime

internal class KlagebehandlingListMapperTest {

    val fnr1 = "01011012345"
    val fnr2 = "02022012345"

    val klagebehandling1 = EsKlagebehandling(
        id = "1001L",
        sakenGjelderFnr = fnr1,
        tildeltEnhet = "4219",
        tema = Tema.OMS.id,
        ytelseId = Ytelse.OMS_OMP.id,
        type = Type.KLAGE.id,
        innsendt = LocalDate.of(2019, 10, 1),
        mottattFoersteinstans = LocalDate.of(2019, 11, 1),
        mottattKlageinstans = LocalDateTime.of(2019, 12, 1, 0, 0),
        frist = LocalDate.of(2020, 12, 1),
        hjemler = listOf(),
        created = LocalDateTime.now(),
        modified = LocalDateTime.now(),
        kilde = "K9",
        status = EsKlagebehandling.Status.IKKE_TILDELT,
        medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
    )
    val klagebehandling2 =
        EsKlagebehandling(
            id = "1002L",
            sakenGjelderFnr = fnr1,
            tildeltEnhet = "4219",
            tema = Tema.SYK.id,
            ytelseId = Ytelse.OMS_OMP.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2018, 10, 1),
            mottattFoersteinstans = LocalDate.of(2018, 11, 1),
            mottattKlageinstans = LocalDateTime.of(2018, 12, 1, 0, 0),
            frist = LocalDate.of(2019, 12, 1),
            hjemler = listOf(),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            status = EsKlagebehandling.Status.IKKE_TILDELT,
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
        )
    val klagebehandling3 =
        EsKlagebehandling(
            id = "1003L",
            sakenGjelderFnr = fnr2,
            tildeltEnhet = "4219",
            tema = Tema.SYK.id,
            ytelseId = Ytelse.OMS_OMP.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2018, 10, 1),
            mottattFoersteinstans = LocalDate.of(2018, 11, 1),
            mottattKlageinstans = LocalDateTime.of(2018, 12, 1, 0, 0),
            frist = LocalDate.of(2019, 12, 1),
            hjemler = listOf(),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            status = EsKlagebehandling.Status.IKKE_TILDELT,
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
        )
}
