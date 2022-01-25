package no.nav.klage.search.api.mapper


import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.search.api.view.FnrSearchResponse
import no.nav.klage.search.api.view.BehandlingListView
import no.nav.klage.search.api.view.NavnView
import no.nav.klage.search.api.view.PersonView
import no.nav.klage.search.clients.pdl.Sivilstand
import no.nav.klage.search.domain.elasticsearch.EsAnonymBehandling
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class BehandlingListMapper(
    private val accessMapper: AccessMapper,
    private val oAuthTokenService: OAuthTokenService
) {

    fun mapPersonSearchResponseToFnrSearchResponse(
        personSearchResponse: PersonSearchResponse,
    ): FnrSearchResponse {
        val behandlinger =
            mapAnonymeEsBehandlingerToListView(
                esBehandlinger = personSearchResponse.behandlinger,
            )
        return FnrSearchResponse(
            fnr = personSearchResponse.fnr,
            navn = NavnView(
                fornavn = personSearchResponse.fornavn,
                mellomnavn = personSearchResponse.mellomnavn,
                etternavn = personSearchResponse.etternavn
            ),
            klagebehandlinger = behandlinger,
            aapneKlagebehandlinger = behandlinger.filter { !it.isAvsluttetAvSaksbehandler },
            avsluttedeKlagebehandlinger = behandlinger.filter { it.isAvsluttetAvSaksbehandler },
            behandlinger = behandlinger,
            aapneBehandlinger = behandlinger.filter { !it.isAvsluttetAvSaksbehandler },
            avsluttedeBehandlinger = behandlinger.filter { it.isAvsluttetAvSaksbehandler }
        )
    }

    fun mapEsBehandlingerToListView(
        esBehandlinger: List<EsBehandling>,
        visePersonData: Boolean,
        sivilstand: Sivilstand? = null
    ): List<BehandlingListView> {
        return esBehandlinger.map { esBehandling ->
            BehandlingListView(
                id = esBehandling.id,
                person = if (visePersonData) {
                    PersonView(
                        esBehandling.sakenGjelderFnr,
                        esBehandling.sakenGjelderNavn,
                        if (esBehandling.sakenGjelderFnr == sivilstand?.foedselsnr) sivilstand?.type?.id else null
                    )
                } else {
                    null
                },
                type = esBehandling.type,
                tema = esBehandling.tema,
                ytelse = esBehandling.ytelseId,
                hjemmel = esBehandling.hjemler.firstOrNull(),
                frist = esBehandling.frist,
                mottatt = esBehandling.sakMottattKaDato!!.toLocalDate(),
                harMedunderskriver = esBehandling.medunderskriverident != null,
                erMedunderskriver = esBehandling.medunderskriverident != null && esBehandling.medunderskriverident == oAuthTokenService.getInnloggetIdent(),
                medunderskriverident = esBehandling.medunderskriverident,
                medunderskriverFlyt = MedunderskriverFlyt.valueOf(esBehandling.medunderskriverFlyt),
                erTildelt = esBehandling.tildeltSaksbehandlerident != null,
                tildeltSaksbehandlerident = esBehandling.tildeltSaksbehandlerident,
                tildeltSaksbehandlerNavn = esBehandling.tildeltSaksbehandlernavn,
                utfall = esBehandling.vedtakUtfall,
                avsluttetAvSaksbehandlerDate = esBehandling.avsluttetAvSaksbehandler?.toLocalDate(),
                isAvsluttetAvSaksbehandler = esBehandling.avsluttetAvSaksbehandler?.toLocalDate() != null,
                saksbehandlerHarTilgang = accessMapper.kanTildelesOppgaven(esBehandling),
                egenAnsatt = esBehandling.egenAnsatt,
                fortrolig = esBehandling.fortrolig,
                strengtFortrolig = esBehandling.strengtFortrolig,
                ageKA = esBehandling.mottattKlageinstans.toAgeInDays(),
                access = accessMapper.mapAccess(esBehandling),
            )
        }
    }

    fun mapAnonymeEsBehandlingerToListView(
        esBehandlinger: List<EsAnonymBehandling>,
    ): List<BehandlingListView> {
        return esBehandlinger.map { esBehandling ->
            BehandlingListView(
                id = esBehandling.id,
                person = null,
                type = esBehandling.type,
                tema = esBehandling.tema,
                ytelse = esBehandling.ytelseId,
                hjemmel = esBehandling.hjemler.firstOrNull(),
                frist = esBehandling.frist,
                mottatt = esBehandling.mottattKlageinstans.toLocalDate(),
                harMedunderskriver = esBehandling.medunderskriverident != null,
                erMedunderskriver = esBehandling.medunderskriverident != null && esBehandling.medunderskriverident == oAuthTokenService.getInnloggetIdent(),
                medunderskriverident = esBehandling.medunderskriverident,
                medunderskriverFlyt = MedunderskriverFlyt.valueOf(esBehandling.medunderskriverFlyt),
                erTildelt = esBehandling.tildeltSaksbehandlerident != null,
                tildeltSaksbehandlerident = esBehandling.tildeltSaksbehandlerident,
                tildeltSaksbehandlerNavn = esBehandling.tildeltSaksbehandlernavn,
                utfall = esBehandling.vedtakUtfall,
                avsluttetAvSaksbehandlerDate = esBehandling.avsluttetAvSaksbehandler?.toLocalDate(),
                isAvsluttetAvSaksbehandler = esBehandling.avsluttetAvSaksbehandler?.toLocalDate() != null,
                saksbehandlerHarTilgang = accessMapper.kanTildelesOppgaven(esBehandling),
                egenAnsatt = esBehandling.egenAnsatt,
                fortrolig = esBehandling.fortrolig,
                strengtFortrolig = esBehandling.strengtFortrolig,
                ageKA = esBehandling.mottattKlageinstans.toAgeInDays(),
                access = accessMapper.mapAccess(esBehandling)
            )
        }
    }


    private fun LocalDateTime.toAgeInDays() = ChronoUnit.DAYS.between(this.toLocalDate(), LocalDate.now()).toInt()
}