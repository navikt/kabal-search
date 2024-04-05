package no.nav.klage.search.service

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.search.api.mapper.BehandlingListMapper
import no.nav.klage.search.api.mapper.BehandlingerSearchCriteriaMapper
import no.nav.klage.search.api.view.AntallUtgaatteFristerResponse
import no.nav.klage.search.api.view.BehandlingerListResponse
import no.nav.klage.search.api.view.MineLedigeOppgaverCountQueryParams
import no.nav.klage.search.api.view.MineLedigeOppgaverQueryParams
import org.springframework.stereotype.Service

@Service
class OppgaverService(
    private val kabalInnstillingerService: KabalInnstillingerService,
    private val behandlingerSearchCriteriaMapper: BehandlingerSearchCriteriaMapper,
    private val elasticsearchService: ElasticsearchService,
    private val behandlingListMapper: BehandlingListMapper,
) {
    fun getLedigeOppgaverForInnloggetSaksbehandler(
        queryParams: MineLedigeOppgaverQueryParams,
    ): BehandlingerListResponse {
        val saksbehandlerInnstillinger = kabalInnstillingerService.getInnstillingerForCurrentSaksbehandler()

        val ytelser = getYtelserQueryListForSaksbehandler(
            queryYtelser = queryParams.ytelser,
            innstillingerYtelser = saksbehandlerInnstillinger.ytelser
        )

        val hjemler = getHjemlerQueryListForSaksbehandler(
            queryHjemler = queryParams.hjemler,
            innstillingerHjemler = saksbehandlerInnstillinger.hjemler
        )

        val typer = queryParams.typer.ifEmpty {
            listOf(Type.KLAGE.id, Type.ANKE.id)
        }

        val searchCriteria = behandlingerSearchCriteriaMapper.toLedigeOppgaverSearchCriteria(
            queryParams = queryParams.copy(
                ytelser = ytelser,
                hjemler = hjemler,
                typer = typer,
            ),
        )

        val esResponse = elasticsearchService.findLedigeOppgaverByCriteria(criteria = searchCriteria)
        return BehandlingerListResponse(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            behandlinger = behandlingListMapper.mapEsBehandlingerToListView(
                esBehandlinger = esResponse.searchHits.map { it.content },
            )
        )
    }

    fun getUtgaatteFristerAvailableToSaksbehandlerCount(
        queryParams: MineLedigeOppgaverCountQueryParams
    ): AntallUtgaatteFristerResponse {
        val saksbehandlerInnstillinger = kabalInnstillingerService.getInnstillingerForCurrentSaksbehandler()

        val ytelser = getYtelserQueryListForSaksbehandler(
            queryYtelser = queryParams.ytelser,
            innstillingerYtelser = saksbehandlerInnstillinger.ytelser
        )

        val hjemler = getHjemlerQueryListForSaksbehandler(
            queryHjemler = queryParams.hjemler,
            innstillingerHjemler = saksbehandlerInnstillinger.hjemler
        )

        val typer = queryParams.typer.ifEmpty {
            listOf(Type.KLAGE.id, Type.ANKE.id)
        }

        val searchCriteria = behandlingerSearchCriteriaMapper.toSearchCriteriaForLedigeMedUtgaattFrist(
            queryParams = queryParams.copy(
                ytelser = ytelser,
                hjemler = hjemler,
                typer = typer,
            ),
        )

        val esResponse = elasticsearchService.countLedigeOppgaverMedUtgaattFristByCriteria(
            criteria = searchCriteria
        )

        return AntallUtgaatteFristerResponse(
            antall = esResponse
        )
    }

    private fun getYtelserQueryListForSaksbehandler(
        queryYtelser: List<String>,
        innstillingerYtelser: List<Ytelse>
    ): List<String> {
        return if (queryYtelser.isEmpty()) {
            innstillingerYtelser.map { it.id }
        } else {
            innstillingerYtelser.map { it.id }.intersect(queryYtelser.toSet())
        }.toList()
    }

    private fun getHjemlerQueryListForSaksbehandler(
        queryHjemler: List<String>,
        innstillingerHjemler: List<Hjemmel>
    ): List<String> {
        return if (queryHjemler.isEmpty()) {
            innstillingerHjemler.map { it.id }
        } else {
            innstillingerHjemler.map { it.id }.intersect(queryHjemler.toSet())
        }.toList()
    }
}