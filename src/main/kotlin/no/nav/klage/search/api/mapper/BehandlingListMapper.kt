package no.nav.klage.search.api.mapper


import no.nav.klage.kodeverk.FlowState
import no.nav.klage.search.api.view.BehandlingView
import no.nav.klage.search.api.view.FnrSearchResponseWithoutPerson
import no.nav.klage.search.api.view.SattPaaVent
import no.nav.klage.search.domain.elasticsearch.EsAnonymBehandling
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.personsoek.PersonSearchResponse
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class BehandlingListMapper {

    fun mapPersonSearchResponseToFnrSearchResponseWithoutPerson(
        personSearchResponse: PersonSearchResponse,
    ): FnrSearchResponseWithoutPerson {
        val behandlinger = personSearchResponse.behandlinger

        return FnrSearchResponseWithoutPerson(
            aapneBehandlinger = mapEsBehandlingerToListView(behandlinger.filter { it.feilregistrert == null && it.avsluttetAvSaksbehandler == null && it.sattPaaVent == null }),
            avsluttedeBehandlinger = mapEsBehandlingerToListView(behandlinger.filter { it.feilregistrert == null && it.avsluttetAvSaksbehandler != null }),
            feilregistrerteBehandlinger = mapEsBehandlingerToListView(behandlinger.filter { it.feilregistrert != null }
                .sortedByDescending { it.feilregistrert }),
            paaVentBehandlinger = mapEsBehandlingerToListView(behandlinger.filter { it.feilregistrert == null && it.avsluttetAvSaksbehandler == null && it.sattPaaVent != null }),
        )
    }

    fun mapEsBehandlingerToBehandlingView(
        esBehandlinger: List<EsBehandling>,
    ): List<BehandlingView> {
        return esBehandlinger.map { esBehandling ->
            BehandlingView(
                id = esBehandling.behandlingId,
                typeId = esBehandling.typeId,
                ytelseId = esBehandling.ytelseId,
                hjemmelId = esBehandling.hjemmelIdList.firstOrNull(),
                hjemmelIdList = esBehandling.hjemmelIdList,
                frist = esBehandling.frist,
                mottatt = esBehandling.sakMottattKaDato.toLocalDate(),
                tildeltSaksbehandlerident = esBehandling.tildeltSaksbehandlerident,
                utfallId = esBehandling.utfallId,
                avsluttetAvSaksbehandlerDate = esBehandling.avsluttetAvSaksbehandler?.toLocalDate(),
                isAvsluttetAvSaksbehandler = esBehandling.avsluttetAvSaksbehandler?.toLocalDate() != null,
                ageKA = esBehandling.sakMottattKaDato.toAgeInDays(),
                sattPaaVent = esBehandling.toSattPaaVent(),
                feilregistrert = esBehandling.feilregistrert,
                fagsystemId = esBehandling.fagsystemId,
                saksnummer = esBehandling.saksnummer,
                medunderskriver = esBehandling.toMedunderskriverView(),
                rol = esBehandling.toROLView(),
            )
        }
    }

    private fun EsBehandling.toROLView(): BehandlingView.CombinedMedunderskriverAndROLView {
        return BehandlingView.CombinedMedunderskriverAndROLView(
            navIdent = rolIdent,
            flowState = FlowState.of(rolFlowStateId),
            returnertDate = returnertFraROL?.toLocalDate(),
        )
    }

    private fun EsBehandling.toMedunderskriverView(): BehandlingView.CombinedMedunderskriverAndROLView {
        return BehandlingView.CombinedMedunderskriverAndROLView(
            navIdent = medunderskriverident,
            flowState = FlowState.of(medunderskriverFlowStateId),
            returnertDate = null,
        )
    }

    fun mapEsBehandlingerToListView(
        esBehandlinger: List<EsAnonymBehandling>,
    ): List<String> {
        return esBehandlinger.map { it.behandlingId }
    }

    private fun LocalDateTime.toAgeInDays() = ChronoUnit.DAYS.between(this.toLocalDate(), LocalDate.now()).toInt()

    private fun EsBehandling.toSattPaaVent(): SattPaaVent? {
        return if (sattPaaVent != null) {
            SattPaaVent(
                from = sattPaaVent,
                to = sattPaaVentExpires!!,
                isExpired = sattPaaVentExpires.isBefore(LocalDate.now()),
                reason = sattPaaVentReason!!
            )
        } else null
    }
}