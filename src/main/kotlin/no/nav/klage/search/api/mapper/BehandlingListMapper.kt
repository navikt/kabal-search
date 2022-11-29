package no.nav.klage.search.api.mapper


import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.search.api.view.*
import no.nav.klage.search.clients.pdl.Sivilstand
import no.nav.klage.search.domain.elasticsearch.EsAnonymBehandling
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import no.nav.klage.search.service.saksbehandler.InnloggetSaksbehandlerService
import no.nav.klage.search.service.saksbehandler.OAuthTokenService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.ChronoLocalDateTime
import java.time.temporal.ChronoUnit

@Component
class BehandlingListMapper(
    private val accessMapper: AccessMapper,
    private val oAuthTokenService: OAuthTokenService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
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

        val kanBehandleStrengtFortrolig = oAuthTokenService.kanBehandleStrengtFortrolig()
        val kanBehandleFortrolig = oAuthTokenService.kanBehandleFortrolig()
        val kanBehandleEgenAnsatt = oAuthTokenService.kanBehandleEgenAnsatt()
        val lovligeYtelser = innloggetSaksbehandlerService.getEnhetMedYtelserForSaksbehandler().ytelser
        val innloggetIdent = oAuthTokenService.getInnloggetIdent()

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
                erMedunderskriver = esBehandling.medunderskriverident != null && esBehandling.medunderskriverident == innloggetIdent,
                medunderskriverident = esBehandling.medunderskriverident,
                medunderskriverNavn = esBehandling.medunderskriverNavn,
                medunderskriverFlyt = MedunderskriverFlyt.valueOf(esBehandling.medunderskriverFlyt),
                erTildelt = esBehandling.tildeltSaksbehandlerident != null,
                tildeltSaksbehandlerident = esBehandling.tildeltSaksbehandlerident,
                tildeltSaksbehandlerNavn = esBehandling.tildeltSaksbehandlernavn,
                utfall = esBehandling.vedtakUtfall,
                avsluttetAvSaksbehandlerDate = esBehandling.avsluttetAvSaksbehandler?.toLocalDate(),
                isAvsluttetAvSaksbehandler = esBehandling.avsluttetAvSaksbehandler?.toLocalDate() != null,
                saksbehandlerHarTilgang = accessMapper.kanTildelesOppgaven(
                    esKlagebehandling = esBehandling,
                    kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig,
                    kanBehandleFortrolig = kanBehandleFortrolig,
                    kanBehandleEgenAnsatt = kanBehandleEgenAnsatt,
                    lovligeYtelser = lovligeYtelser
                ),
                egenAnsatt = esBehandling.egenAnsatt,
                fortrolig = esBehandling.fortrolig,
                strengtFortrolig = esBehandling.strengtFortrolig,
                ageKA = esBehandling.mottattKlageinstans.toAgeInDays(),
                access = accessMapper.mapAccess(
                    esKlagebehandling = esBehandling,
                    innloggetIdent = innloggetIdent,
                    kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig,
                    kanBehandleFortrolig = kanBehandleFortrolig,
                    kanBehandleEgenAnsatt = kanBehandleEgenAnsatt,
                    lovligeYtelser = lovligeYtelser,
                ),
                sattPaaVent = esBehandling.toSattPaaVent(),
            )
        }
    }

    fun mapAnonymeEsBehandlingerToListView(
        esBehandlinger: List<EsAnonymBehandling>,
    ): List<BehandlingListView> {

        val kanBehandleStrengtFortrolig = oAuthTokenService.kanBehandleStrengtFortrolig()
        val kanBehandleFortrolig = oAuthTokenService.kanBehandleFortrolig()
        val kanBehandleEgenAnsatt = oAuthTokenService.kanBehandleEgenAnsatt()
        val lovligeYtelser = innloggetSaksbehandlerService.getEnhetMedYtelserForSaksbehandler().ytelser
        val innloggetIdent = oAuthTokenService.getInnloggetIdent()

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
                erMedunderskriver = esBehandling.medunderskriverident != null && esBehandling.medunderskriverident == innloggetIdent,
                medunderskriverident = esBehandling.medunderskriverident,
                medunderskriverNavn = esBehandling.medunderskriverNavn,
                medunderskriverFlyt = MedunderskriverFlyt.valueOf(esBehandling.medunderskriverFlyt),
                erTildelt = esBehandling.tildeltSaksbehandlerident != null,
                tildeltSaksbehandlerident = esBehandling.tildeltSaksbehandlerident,
                tildeltSaksbehandlerNavn = esBehandling.tildeltSaksbehandlernavn,
                utfall = esBehandling.vedtakUtfall,
                avsluttetAvSaksbehandlerDate = esBehandling.avsluttetAvSaksbehandler?.toLocalDate(),
                isAvsluttetAvSaksbehandler = esBehandling.avsluttetAvSaksbehandler?.toLocalDate() != null,
                saksbehandlerHarTilgang = accessMapper.kanTildelesOppgaven(
                    esKlagebehandling = esBehandling,
                    kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig,
                    kanBehandleFortrolig = kanBehandleFortrolig,
                    kanBehandleEgenAnsatt = kanBehandleEgenAnsatt,
                    lovligeYtelser = lovligeYtelser
                ),
                egenAnsatt = esBehandling.egenAnsatt,
                fortrolig = esBehandling.fortrolig,
                strengtFortrolig = esBehandling.strengtFortrolig,
                ageKA = esBehandling.mottattKlageinstans.toAgeInDays(),
                access = accessMapper.mapAccess(
                    esKlagebehandling = esBehandling,
                    innloggetIdent = innloggetIdent,
                    kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig,
                    kanBehandleFortrolig = kanBehandleFortrolig,
                    kanBehandleEgenAnsatt = kanBehandleEgenAnsatt,
                    lovligeYtelser = lovligeYtelser,
                ),
                sattPaaVent = esBehandling.toSattPaaVent(),
            )
        }
    }

    private fun LocalDateTime.toAgeInDays() = ChronoUnit.DAYS.between(this.toLocalDate(), LocalDate.now()).toInt()

    private fun EsBehandling.toSattPaaVent(): Venteperiode? {
        return if (sattPaaVent != null) {
            Venteperiode(
                from = sattPaaVent.toLocalDate(),
                to = sattPaaVentExpires?.toLocalDate(),
                isExpired = sattPaaVentExpires?.isBefore(ChronoLocalDateTime.from(LocalDateTime.now()))
            )
        } else null
    }

    private fun EsAnonymBehandling.toSattPaaVent(): Venteperiode? {
        return if (sattPaaVent != null) {
            Venteperiode(
                from = sattPaaVent!!.toLocalDate(),
                to = sattPaaVentExpires?.toLocalDate(),
                isExpired = sattPaaVentExpires?.isBefore(ChronoLocalDateTime.from(LocalDateTime.now()))
            )
        } else null
    }
}