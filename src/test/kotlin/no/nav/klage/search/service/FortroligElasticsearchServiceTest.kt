package no.nav.klage.search.service

import no.nav.klage.kodeverk.FlowState
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.LedigeOppgaverSearchCriteria
import no.nav.klage.search.domain.SortField
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.elasticsearch.EsStatus.IKKE_TILDELT
import no.nav.klage.search.repositories.EsBehandlingRepository
import no.nav.klage.search.repositories.SearchHits
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.opensearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
class FortroligElasticsearchServiceTest {

    companion object {
        @Container
        @JvmField
        val esContainer: TestOpenSearchContainer = TestOpenSearchContainer.instance
    }

    @Autowired
    lateinit var service: ElasticsearchService

    @Autowired
    lateinit var repo: EsBehandlingRepository

    @Test
    @Order(1)
    fun `es is running`() {
        assertThat(esContainer.isRunning).isTrue
        service.recreateIndex()
    }

    @Test
    @Order(2)
    fun `index has been created by service`() {
        assertThat(repo.indexExists()).isTrue()
    }

    private val idNormal = "1001L"

    private val idFortrolig = "1002L"

    private val idStrengtFortrolig = "1003L"

    private val idEgenAnsatt = "1004L"

    private val idEgenAnsattOgFortrolig = "1005L"

    private val idEgenAnsattOgStrengtFortrolig = "1006L"

    @Test
    @Order(3)
    fun `lagrer oppgaver for senere tester`() {

        val normalPerson = EsBehandling(
            behandlingId = idNormal,
            tildeltEnhet = "4219",
            ytelseId = Ytelse.OMS_OMP.id,
            typeId = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2019, 10, 1),
            sakMottattKaDato = LocalDateTime.of(2019, 12, 1, 0, 0),
            frist = LocalDate.of(2020, 12, 1),
            hjemmelIdList = listOf(),
            status = IKKE_TILDELT,
            sakenGjelderFnr = "123",
            medunderskriverFlowStateId = FlowState.NOT_SENT.id,
            fagsystemId = "1",
            rolIdent = "ROLIDENT",
            rolFlowStateId = "1",
            saksnummer = "123",
            avsluttetAvSaksbehandler = null,
            returnertFraROL = null,
            tildeltSaksbehandlernavn = null,
            medunderskriverNavn = null,
            medunderskriverEnhet = null,
            medunderskriverident = null,
        )
        val fortroligPerson =
            EsBehandling(
                behandlingId = idFortrolig,
                tildeltEnhet = "4219",
                ytelseId = Ytelse.OMS_OMP.id,
                typeId = Type.KLAGE.id,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2018, 10, 1),
                sakMottattKaDato = LocalDateTime.of(2018, 12, 1, 0, 0),
                frist = LocalDate.of(2019, 12, 1),
                hjemmelIdList = listOf(),
                status = IKKE_TILDELT,
                sakenGjelderFnr = "123",
                fortrolig = true,
                medunderskriverFlowStateId = FlowState.NOT_SENT.id,
                fagsystemId = "1",
                rolIdent = "ROLIDENT",
                rolFlowStateId = "1",
                saksnummer = "123",
                avsluttetAvSaksbehandler = null,
                returnertFraROL = null,
                tildeltSaksbehandlernavn = null,
                medunderskriverNavn = null,
                medunderskriverEnhet = null,
                medunderskriverident = null,
            )
        val strengtFortroligPerson = EsBehandling(
            behandlingId = idStrengtFortrolig,
            tildeltEnhet = "4219",
            ytelseId = Ytelse.OMS_OMP.id,
            typeId = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2019, 10, 1),
            sakMottattKaDato = LocalDateTime.of(2019, 12, 1, 0, 0),
            frist = LocalDate.of(2020, 12, 1),
            hjemmelIdList = listOf(),
            status = IKKE_TILDELT,
            sakenGjelderFnr = "123",
            strengtFortrolig = true,
            medunderskriverFlowStateId = FlowState.NOT_SENT.id,
            fagsystemId = "1",
            rolIdent = "ROLIDENT",
            rolFlowStateId = "1",
            saksnummer = "123",
            avsluttetAvSaksbehandler = null,
            returnertFraROL = null,
            tildeltSaksbehandlernavn = null,
            medunderskriverNavn = null,
            medunderskriverEnhet = null,
            medunderskriverident = null,
        )
        val egenAnsattPerson =
            EsBehandling(
                behandlingId = idEgenAnsatt,
                tildeltEnhet = "4219",
                ytelseId = Ytelse.OMS_OMP.id,
                typeId = Type.KLAGE.id,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2018, 10, 1),
                sakMottattKaDato = LocalDateTime.of(2018, 12, 1, 0, 0),
                frist = LocalDate.of(2019, 12, 1),
                hjemmelIdList = listOf(),
                status = IKKE_TILDELT,
                sakenGjelderFnr = "123",
                egenAnsatt = true,
                medunderskriverFlowStateId = FlowState.NOT_SENT.id,
                fagsystemId = "1",
                rolIdent = "ROLIDENT",
                rolFlowStateId = "1",
                saksnummer = "123",
                avsluttetAvSaksbehandler = null,
                returnertFraROL = null,
                tildeltSaksbehandlernavn = null,
                medunderskriverNavn = null,
                medunderskriverEnhet = null,
                medunderskriverident = null,
            )
        val egenAnsattOgFortroligPerson =
            EsBehandling(
                behandlingId = idEgenAnsattOgFortrolig,
                tildeltEnhet = "4219",
                ytelseId = Ytelse.OMS_OMP.id,
                typeId = Type.KLAGE.id,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2018, 10, 1),
                sakMottattKaDato = LocalDateTime.of(2018, 12, 1, 0, 0),
                frist = LocalDate.of(2019, 12, 1),
                hjemmelIdList = listOf(),
                status = IKKE_TILDELT,
                sakenGjelderFnr = "123",
                egenAnsatt = true,
                fortrolig = true,
                medunderskriverFlowStateId = FlowState.NOT_SENT.id,
                fagsystemId = "1",
                rolIdent = "ROLIDENT",
                rolFlowStateId = "1",
                saksnummer = "123",
                avsluttetAvSaksbehandler = null,
                returnertFraROL = null,
                tildeltSaksbehandlernavn = null,
                medunderskriverNavn = null,
                medunderskriverEnhet = null,
                medunderskriverident = null,
            )
        val egenAnsattOgStrengtFortroligPerson =
            EsBehandling(
                behandlingId = idEgenAnsattOgStrengtFortrolig,
                tildeltEnhet = "4219",
                ytelseId = Ytelse.OMS_OMP.id,
                typeId = Type.KLAGE.id,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2018, 10, 1),
                sakMottattKaDato = LocalDateTime.of(2018, 12, 1, 0, 0),
                frist = LocalDate.of(2019, 12, 1),
                hjemmelIdList = listOf(),
                status = IKKE_TILDELT,
                sakenGjelderFnr = "123",
                egenAnsatt = true,
                strengtFortrolig = true,
                medunderskriverFlowStateId = FlowState.NOT_SENT.id,
                fagsystemId = "1",
                rolIdent = "ROLIDENT",
                rolFlowStateId = "1",
                saksnummer = "123",
                avsluttetAvSaksbehandler = null,
                returnertFraROL = null,
                tildeltSaksbehandlernavn = null,
                medunderskriverNavn = null,
                medunderskriverEnhet = null,
                medunderskriverident = null,
            )
        repo.save(normalPerson)
        repo.save(fortroligPerson)
        repo.save(strengtFortroligPerson)
        repo.save(egenAnsattPerson)
        repo.save(egenAnsattOgFortroligPerson)
        repo.save(egenAnsattOgStrengtFortroligPerson)

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsBehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(6L)
    }

    @Test
    @Order(4)
    fun `Saksbehandler with no special rights will only see normal klagebehandlinger`() {
        val klagebehandlinger: List<EsBehandling> =
            service.findLedigeOppgaverByCriteria(
                LedigeOppgaverSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    typer = emptyList(),
                    hjemler = emptyList(),
                    offset = 0,
                    limit = 10,
                    order = no.nav.klage.search.domain.Order.ASC,
                    sortField = SortField.FRIST,
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = false,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.behandlingId }).containsExactlyInAnyOrder(idNormal)
    }

    @Test
    @Order(5)
    fun `Saksbehandler with egen ansatt rights will only see normal klagebehandlinger and those for egen ansatte, but not egen ansatte that are fortrolig or strengt fortrolig`() {
        val klagebehandlinger: List<EsBehandling> =
            service.findLedigeOppgaverByCriteria(
                LedigeOppgaverSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    typer = emptyList(),
                    hjemler = emptyList(),
                    offset = 0,
                    limit = 10,
                    order = no.nav.klage.search.domain.Order.ASC,
                    sortField = SortField.FRIST,
                    kanBehandleEgenAnsatt = true,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = false,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.behandlingId }).containsExactlyInAnyOrder(idNormal, idEgenAnsatt)
    }

    @Test
    @Order(6)
    fun `Saksbehandler with fortrolig rights will see normale klagebehandlinger and fortrolige klagebehandlinger, including the combo fortrolig and egen ansatt`() {
        val klagebehandlinger: List<EsBehandling> =
            service.findLedigeOppgaverByCriteria(
                LedigeOppgaverSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    typer = emptyList(),
                    hjemler = emptyList(),
                    offset = 0,
                    limit = 10,
                    order = no.nav.klage.search.domain.Order.ASC,
                    sortField = SortField.FRIST,
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = true,
                    kanBehandleStrengtFortrolig = false,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.behandlingId }).containsExactlyInAnyOrder(
            idNormal, idFortrolig,
            idEgenAnsattOgFortrolig
        )
    }

    @Test
    @Order(7)
    fun `Saksbehandler with fortrolig rights and egen ansatt rights will see normale klagebehandling, fortrolige klagebehandlinger and egen ansatt klagebehandlinger`() {
        val klagebehandlinger: List<EsBehandling> =
            service.findLedigeOppgaverByCriteria(
                LedigeOppgaverSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    typer = emptyList(),
                    hjemler = emptyList(),
                    offset = 0,
                    limit = 10,
                    order = no.nav.klage.search.domain.Order.ASC,
                    sortField = SortField.FRIST,
                    kanBehandleEgenAnsatt = true,
                    kanBehandleFortrolig = true,
                    kanBehandleStrengtFortrolig = false,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.behandlingId }).containsExactlyInAnyOrder(
            idNormal, idFortrolig, idEgenAnsatt,
            idEgenAnsattOgFortrolig
        )
    }

    @Test
    @Order(8)
    fun `Saksbehandler with strengt fortrolig rights and egen ansatt rights will see strengt fortrolige klagebehandlinger, including the combo strengt fortrolig and egen ansatt`() {
        val klagebehandlinger: List<EsBehandling> =
            service.findLedigeOppgaverByCriteria(
                LedigeOppgaverSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    typer = emptyList(),
                    hjemler = emptyList(),
                    offset = 0,
                    limit = 10,
                    order = no.nav.klage.search.domain.Order.ASC,
                    sortField = SortField.FRIST,
                    kanBehandleEgenAnsatt = true,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = true,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.behandlingId }).containsExactlyInAnyOrder(
            idStrengtFortrolig, idEgenAnsattOgStrengtFortrolig
        )
    }

    @Test
    @Order(9)
    fun `Saksbehandler with strengt fortrolig rights without egen ansatt rights will only see strengt fortrolige klagebehandlinger, including the combo strengt fortrolig and egen ansatt`() {
        val klagebehandlinger: List<EsBehandling> =
            service.findLedigeOppgaverByCriteria(
                LedigeOppgaverSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    typer = emptyList(),
                    hjemler = emptyList(),
                    offset = 0,
                    limit = 10,
                    order = no.nav.klage.search.domain.Order.ASC,
                    sortField = SortField.FRIST,
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = true,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.behandlingId }).containsExactlyInAnyOrder(
            idStrengtFortrolig,
            idEgenAnsattOgStrengtFortrolig
        )
    }

    @Test
    @Order(10)
    fun `Saksbehandler with fortrolig and strengt fortrolig rights will only see strengt fortrolige and fortrolige klagebehandlinger, including those that also are egen ansatte`() {
        val klagebehandlinger: List<EsBehandling> =
            service.findLedigeOppgaverByCriteria(
                LedigeOppgaverSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    typer = emptyList(),
                    hjemler = emptyList(),
                    offset = 0,
                    limit = 10,
                    order = no.nav.klage.search.domain.Order.ASC,
                    sortField = SortField.FRIST,
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = true,
                    kanBehandleStrengtFortrolig = true,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.behandlingId }).containsExactlyInAnyOrder(
            idStrengtFortrolig,
            idEgenAnsattOgStrengtFortrolig,
            idFortrolig,
            idEgenAnsattOgFortrolig
        )
    }

    @Test
    @Order(11)
    fun `Saksbehandler with fortrolig and strengt fortrolig and egen ansatt rights will only see strengt fortrolige and fortrolige klagebehandlinger, including those that also are egen ansatte`() {
        val klagebehandlinger: List<EsBehandling> =
            service.findLedigeOppgaverByCriteria(
                LedigeOppgaverSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    typer = emptyList(),
                    hjemler = emptyList(),
                    offset = 0,
                    limit = 10,
                    order = no.nav.klage.search.domain.Order.ASC,
                    sortField = SortField.FRIST,
                    kanBehandleEgenAnsatt = true,
                    kanBehandleFortrolig = true,
                    kanBehandleStrengtFortrolig = true,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.behandlingId }).containsExactlyInAnyOrder(
            idStrengtFortrolig,
            idEgenAnsattOgStrengtFortrolig,
            idFortrolig,
            idEgenAnsattOgFortrolig
        )
    }
}