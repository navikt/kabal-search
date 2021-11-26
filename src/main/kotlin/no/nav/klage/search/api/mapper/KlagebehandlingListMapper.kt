package no.nav.klage.search.api.mapper


import no.nav.klage.search.api.view.FnrSearchResponse
import no.nav.klage.search.api.view.KlagebehandlingListView
import no.nav.klage.search.api.view.NavnView
import no.nav.klage.search.clients.pdl.Sivilstand
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.kodeverk.MedunderskriverFlyt
import no.nav.klage.search.domain.kodeverk.Ytelse
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class KlagebehandlingListMapper {

    fun mapPersonSearchResponseToFnrSearchResponse(
        personSearchResponse: PersonSearchResponse,
        saksbehandler: String,
        tilgangTilYtelser: List<Ytelse>
    ): FnrSearchResponse? {
        val klagebehandlinger =
            mapEsKlagebehandlingerToListView(
                esKlagebehandlinger = personSearchResponse.klagebehandlinger,
                viseUtvidet = false,
                viseFullfoerte = true,
                saksbehandlere = listOf(saksbehandler),
                tilgangTilYtelser = tilgangTilYtelser
            )
        return FnrSearchResponse(
            fnr = personSearchResponse.fnr,
            navn = NavnView(
                fornavn = personSearchResponse.fornavn,
                mellomnavn = personSearchResponse.mellomnavn,
                etternavn = personSearchResponse.etternavn
            ),
            klagebehandlinger = klagebehandlinger,
            aapneKlagebehandlinger = klagebehandlinger.filter { !it.isAvsluttetAvSaksbehandler },
            avsluttedeKlagebehandlinger = klagebehandlinger.filter { it.isAvsluttetAvSaksbehandler }
        )
    }

    fun mapEsKlagebehandlingerToListView(
        esKlagebehandlinger: List<EsKlagebehandling>,
        viseUtvidet: Boolean,
        viseFullfoerte: Boolean,
        saksbehandlere: List<String>,
        tilgangTilYtelser: List<Ytelse>,
        sivilstand: Sivilstand? = null
    ): List<KlagebehandlingListView> {
        return esKlagebehandlinger.map { esKlagebehandling ->
            KlagebehandlingListView(
                id = esKlagebehandling.id,
                person = if (viseUtvidet) {
                    KlagebehandlingListView.Person(
                        esKlagebehandling.sakenGjelderFnr,
                        esKlagebehandling.sakenGjelderNavn,
                        if (esKlagebehandling.sakenGjelderFnr == sivilstand?.foedselsnr) sivilstand?.type?.id else null
                    )
                } else {
                    null
                },
                type = esKlagebehandling.type,
                tema = esKlagebehandling.tema,
                ytelse = esKlagebehandling.ytelseId,
                hjemmel = esKlagebehandling.hjemler.firstOrNull(),
                frist = esKlagebehandling.frist,
                mottatt = esKlagebehandling.mottattKlageinstans.toLocalDate(),
                harMedunderskriver = esKlagebehandling.medunderskriverident != null,
                erMedunderskriver = esKlagebehandling.medunderskriverident != null && esKlagebehandling.medunderskriverident in saksbehandlere,
                medunderskriverident = esKlagebehandling.medunderskriverident,
                medunderskriverFlyt = MedunderskriverFlyt.valueOf(esKlagebehandling.medunderskriverFlyt),
                erTildelt = esKlagebehandling.tildeltSaksbehandlerident != null,
                tildeltSaksbehandlerident = esKlagebehandling.tildeltSaksbehandlerident,
                tildeltSaksbehandlerNavn = esKlagebehandling.tildeltSaksbehandlernavn,
                utfall = if (viseFullfoerte) {
                    esKlagebehandling.vedtakUtfall
                } else {
                    null
                },
                avsluttetAvSaksbehandlerDate = if (viseFullfoerte) {
                    esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate()
                } else {
                    null
                },
                isAvsluttetAvSaksbehandler = esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate() != null,
                //TODO: Burde vi defaulte til true eller false hvis ytelseId == null?
                saksbehandlerHarTilgang = esKlagebehandling.ytelseId == null
                        || tilgangTilYtelser.contains(Ytelse.of(esKlagebehandling.ytelseId)),
                egenAnsatt = esKlagebehandling.egenAnsatt,
                fortrolig = esKlagebehandling.fortrolig,
                strengtFortrolig = esKlagebehandling.strengtFortrolig,
                ageKA = esKlagebehandling.mottattKlageinstans.toAgeInDays()
            )
        }
    }

    private fun LocalDateTime.toAgeInDays() = ChronoUnit.DAYS.between(this.toLocalDate(), LocalDate.now()).toInt()
}