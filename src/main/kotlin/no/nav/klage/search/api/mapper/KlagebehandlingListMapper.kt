package no.nav.klage.search.api.mapper


import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.search.api.view.FnrSearchResponse
import no.nav.klage.search.api.view.KlagebehandlingListView
import no.nav.klage.search.api.view.NavnView
import no.nav.klage.search.api.view.PersonView
import no.nav.klage.search.clients.pdl.Sivilstand
import no.nav.klage.search.domain.elasticsearch.EsAnonymKlagebehandling
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class KlagebehandlingListMapper(
    private val accessMapper: AccessMapper,
    private val oAuthTokenService: OAuthTokenService
) {

    fun mapPersonSearchResponseToFnrSearchResponse(
        personSearchResponse: PersonSearchResponse,
    ): FnrSearchResponse {
        val klagebehandlinger =
            mapAnonymeEsKlagebehandlingerToListView(
                esKlagebehandlinger = personSearchResponse.klagebehandlinger,
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
        visePersonData: Boolean,
        sivilstand: Sivilstand? = null
    ): List<KlagebehandlingListView> {
        return esKlagebehandlinger.map { esKlagebehandling ->
            KlagebehandlingListView(
                id = esKlagebehandling.id,
                person = if (visePersonData) {
                    PersonView(
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
                erMedunderskriver = esKlagebehandling.medunderskriverident != null && esKlagebehandling.medunderskriverident == oAuthTokenService.getInnloggetIdent(),
                medunderskriverident = esKlagebehandling.medunderskriverident,
                medunderskriverFlyt = MedunderskriverFlyt.valueOf(esKlagebehandling.medunderskriverFlyt),
                erTildelt = esKlagebehandling.tildeltSaksbehandlerident != null,
                tildeltSaksbehandlerident = esKlagebehandling.tildeltSaksbehandlerident,
                tildeltSaksbehandlerNavn = esKlagebehandling.tildeltSaksbehandlernavn,
                utfall = esKlagebehandling.vedtakUtfall,
                avsluttetAvSaksbehandlerDate = esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate(),
                isAvsluttetAvSaksbehandler = esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate() != null,
                saksbehandlerHarTilgang = accessMapper.kanTildelesOppgaven(esKlagebehandling),
                egenAnsatt = esKlagebehandling.egenAnsatt,
                fortrolig = esKlagebehandling.fortrolig,
                strengtFortrolig = esKlagebehandling.strengtFortrolig,
                ageKA = esKlagebehandling.mottattKlageinstans.toAgeInDays(),
                access = accessMapper.mapAccess(esKlagebehandling),
            )
        }
    }

    fun mapAnonymeEsKlagebehandlingerToListView(
        esKlagebehandlinger: List<EsAnonymKlagebehandling>,
    ): List<KlagebehandlingListView> {
        return esKlagebehandlinger.map { esKlagebehandling ->
            KlagebehandlingListView(
                id = esKlagebehandling.id,
                person = null,
                type = esKlagebehandling.type,
                tema = esKlagebehandling.tema,
                ytelse = esKlagebehandling.ytelseId,
                hjemmel = esKlagebehandling.hjemler.firstOrNull(),
                frist = esKlagebehandling.frist,
                mottatt = esKlagebehandling.mottattKlageinstans.toLocalDate(),
                harMedunderskriver = esKlagebehandling.medunderskriverident != null,
                erMedunderskriver = esKlagebehandling.medunderskriverident != null && esKlagebehandling.medunderskriverident == oAuthTokenService.getInnloggetIdent(),
                medunderskriverident = esKlagebehandling.medunderskriverident,
                medunderskriverFlyt = MedunderskriverFlyt.valueOf(esKlagebehandling.medunderskriverFlyt),
                erTildelt = esKlagebehandling.tildeltSaksbehandlerident != null,
                tildeltSaksbehandlerident = esKlagebehandling.tildeltSaksbehandlerident,
                tildeltSaksbehandlerNavn = esKlagebehandling.tildeltSaksbehandlernavn,
                utfall = esKlagebehandling.vedtakUtfall,
                avsluttetAvSaksbehandlerDate = esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate(),
                isAvsluttetAvSaksbehandler = esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate() != null,
                saksbehandlerHarTilgang = accessMapper.kanTildelesOppgaven(esKlagebehandling),
                egenAnsatt = esKlagebehandling.egenAnsatt,
                fortrolig = esKlagebehandling.fortrolig,
                strengtFortrolig = esKlagebehandling.strengtFortrolig,
                ageKA = esKlagebehandling.mottattKlageinstans.toAgeInDays(),
                access = accessMapper.mapAccess(esKlagebehandling)
            )
        }
    }


    private fun LocalDateTime.toAgeInDays() = ChronoUnit.DAYS.between(this.toLocalDate(), LocalDate.now()).toInt()
}