package no.nav.klage.search.service

import no.nav.klage.kodeverk.FlowState
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.CountLedigeOppgaverMedUtgaattFristSearchCriteria
import no.nav.klage.search.domain.LedigeOppgaverSearchCriteria
import no.nav.klage.search.domain.SortField
import no.nav.klage.search.domain.elasticsearch.EsBehandling
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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
@DirtiesContext
class ElasticsearchServiceTest {
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

    @Test
    @Order(3)
    fun `lagrer oppgaver for senere tester`() {
        val klagebehandling1 =
            EsBehandling(
                behandlingId = "1001L",
                tildeltEnhet = "4219",
                ytelseId = Ytelse.OMS_OMP.id,
                typeId = Type.KLAGE.id,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2019, 10, 1),
                sakMottattKaDato = LocalDateTime.of(2019, 12, 1, 0, 0),
                sendtTilTrygderetten = null,
                ageStartDate = LocalDate.of(2019, 12, 1),
                frist = LocalDate.of(2020, 12, 1),
                varsletFrist = LocalDate.of(2020, 12, 1),
                hjemmelIdList = listOf(Hjemmel.FTRL_8_35.id, Hjemmel.FTRL_8_34.id),
                medunderskriverFlowStateId = FlowState.NOT_SENT.id,
                sakenGjelderFnr = "12345678910",
                fagsystemId = "1",
                rolIdent = "ROLIDENT",
                rolFlowStateId = "1",
                saksnummer = "123",
                avsluttetAvSaksbehandler = null,
                returnertFraROL = null,
                medunderskriverEnhet = null,
                medunderskriverident = null,
            )
        val klagebehandling2 =
            EsBehandling(
                behandlingId = "1002L",
                tildeltEnhet = "4219",
                ytelseId = Ytelse.SYK_SYK.id,
                typeId = Type.KLAGE.id,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2018, 10, 1),
                sakMottattKaDato = LocalDateTime.of(2018, 12, 1, 0, 0),
                sendtTilTrygderetten = null,
                ageStartDate = LocalDate.of(2019, 12, 1),
                frist = LocalDate.of(2019, 12, 1),
                varsletFrist = LocalDate.of(2020, 12, 1),
                hjemmelIdList = listOf(Hjemmel.FTRL_8_34.id),
                medunderskriverFlowStateId = FlowState.NOT_SENT.id,
                sakenGjelderFnr = "12345678910",
                fagsystemId = "1",
                rolIdent = "ROLIDENT",
                rolFlowStateId = "1",
                saksnummer = "123",
                avsluttetAvSaksbehandler = null,
                returnertFraROL = null,
                medunderskriverEnhet = null,
                medunderskriverident = null,
            )

        val klagebehandling3 =
            EsBehandling(
                behandlingId = "1003L",
                tildeltEnhet = "4219",
                ytelseId = Ytelse.SYK_SYK.id,
                typeId = Type.ANKE_I_TRYGDERETTEN_FOER_2027.id,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2018, 10, 1),
                sakMottattKaDato = LocalDateTime.of(2018, 12, 1, 0, 0),
                sendtTilTrygderetten = LocalDateTime.of(2019, 12, 1, 0, 0),
                ageStartDate = LocalDate.of(2019, 12, 1),
                frist = null,
                varsletFrist = null,
                hjemmelIdList = listOf(Hjemmel.FTRL_8_35.id),
                medunderskriverFlowStateId = FlowState.NOT_SENT.id,
                sakenGjelderFnr = "12345678910",
                fagsystemId = "1",
                rolIdent = "ROLIDENT",
                rolFlowStateId = "1",
                saksnummer = "123",
                avsluttetAvSaksbehandler = null,
                returnertFraROL = null,
                medunderskriverEnhet = null,
                medunderskriverident = null,
            )
        val klagebehandling4 = klagebehandling3.copy(behandlingId = "1004L", hjemmelIdList = emptyList())
        val klagebehandling5 =
            klagebehandling3.copy(
                behandlingId = "1005L",
                hjemmelIdList = listOf(Hjemmel.FTRL_8_35.id, Hjemmel.FTRL_8_34.id, Hjemmel.FTRL_8_36.id),
            )
        val klagebehandling6 =
            klagebehandling2.copy(
                behandlingId = "1006L",
                hjemmelIdList = listOf(Hjemmel.FTRL_8_35.id, Hjemmel.FTRL_8_34.id, Hjemmel.FTRL_8_36.id),
            )
        repo.save(klagebehandling1)
        repo.save(klagebehandling2)
        repo.save(klagebehandling3)
        repo.save(klagebehandling4)
        repo.save(klagebehandling5)
        repo.save(klagebehandling6)

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsBehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(6L)
    }

    @Test
    @Order(4)
    fun `Klagebehandling can be searched for by ytelse`() {
        val klagebehandlinger: List<EsBehandling> =
            service
                .findLedigeOppgaverByCriteria(
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
                        fristFrom = LocalDate.now().minusDays(3650),
                        fristTo = LocalDate.now().plusDays(3650),
                        varsletFristFrom = LocalDate.now().minusDays(3650),
                        varsletFristTo = LocalDate.now().plusDays(3650),
                    ),
                ).searchHits
                .map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.first().behandlingId).isEqualTo("1001L")
    }

    @Test
    @Order(5)
    fun `default hjemmel filter returns all documents with an overlapping hjemmel`() {
        assertThat(findLedigeOppgaverByHjemler(listOf(Hjemmel.FTRL_8_35)))
            .containsExactlyInAnyOrder("1001L", "1003L", "1005L", "1006L")
    }

    @Test
    @Order(6)
    fun `query hjemler use overlap within bounds from innstillinger`() {
        val allowedHjemler = listOf(Hjemmel.FTRL_8_35, Hjemmel.FTRL_8_34)

        assertThat(
            findLedigeOppgaverByHjemlerFromInnstillinger(
                selectedHjemler = listOf(Hjemmel.FTRL_8_35),
                allowedHjemler = allowedHjemler,
            ),
        ).containsExactlyInAnyOrder("1001L", "1003L")
        assertThat(
            findLedigeOppgaverByHjemlerFromInnstillinger(
                selectedHjemler = listOf(Hjemmel.FTRL_8_34),
                allowedHjemler = allowedHjemler,
            ),
        ).containsExactlyInAnyOrder("1001L", "1002L")
        assertThat(findLedigeOppgaverByHjemlerFromInnstillinger(selectedHjemler = allowedHjemler, allowedHjemler = allowedHjemler))
            .containsExactlyInAnyOrder("1001L", "1002L", "1003L")
        assertThat(
            findLedigeOppgaverByHjemlerFromInnstillinger(
                selectedHjemler = emptyList(),
                allowedHjemler = allowedHjemler,
            ),
        ).isEmpty()
    }

    @Test
    @Order(7)
    fun `expired deadline count uses query overlap within bounds from innstillinger`() {
        val count =
            service.countLedigeOppgaverMedUtgaattFristByCriteria(
                criteria =
                    CountLedigeOppgaverMedUtgaattFristSearchCriteria(
                        typer = emptyList(),
                        ytelser = emptyList(),
                        hjemler = listOf(Hjemmel.FTRL_8_35),
                        fristFrom = LocalDate.of(2018, 1, 1),
                        fristTo = LocalDate.now(),
                        varsletFristFrom = LocalDate.of(2018, 1, 1),
                        varsletFristTo = LocalDate.now(),
                        kanBehandleEgenAnsatt = false,
                        kanBehandleFortrolig = false,
                        kanBehandleStrengtFortrolig = false,
                    ),
                innstillingerHjemmelFilter =
                    InnstillingerHjemmelFilter(
                        allowedHjemler = listOf(Hjemmel.FTRL_8_35, Hjemmel.FTRL_8_34),
                    ),
            )

        assertThat(count).isEqualTo(2)
    }

    @Test
    @Order(8)
    fun `Klagebehandling can be searched for by frist`() {
        val antall =
            service.countLedigeOppgaverMedUtgaattFristByCriteria(
                CountLedigeOppgaverMedUtgaattFristSearchCriteria(
                    typer = listOf(Type.KLAGE, Type.ANKE_FOER_2027),
                    ytelser = emptyList(),
                    hjemler = emptyList(),
                    fristFrom = LocalDate.of(2020, 12, 1),
                    fristTo = LocalDate.now(),
                    varsletFristFrom = LocalDate.of(2020, 12, 1),
                    varsletFristTo = LocalDate.now(),
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = false,
                ),
            )
        assertThat(antall).isEqualTo(1L)
    }

    @Test
    @Order(9)
    fun `include frist with null`() {
        val oppgaver =
            service.findLedigeOppgaverByCriteria(
                LedigeOppgaverSearchCriteria(
                    typer = emptyList(),
                    ytelser = emptyList(),
                    hjemler = emptyList(),
                    fristFrom = LocalDate.now().minusDays(3650),
                    fristTo = LocalDate.now().plusDays(3650),
                    varsletFristFrom = LocalDate.now().minusDays(3650),
                    varsletFristTo = LocalDate.now().plusDays(3650),
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = false,
                    sortField = SortField.PAA_VENT_TO,
                    order = no.nav.klage.search.domain.Order.ASC,
                    offset = 0,
                    limit = 1000,
                ),
            )
        assertThat(oppgaver).hasSize(6)
    }

    private fun findLedigeOppgaverByHjemler(hjemler: List<Hjemmel>): List<String> =
        service
            .findLedigeOppgaverByCriteria(
                ledigeOppgaverSearchCriteria(hjemler),
            ).searchHits
            .map { it.content.behandlingId }

    private fun findLedigeOppgaverByHjemlerFromInnstillinger(
        selectedHjemler: List<Hjemmel>,
        allowedHjemler: List<Hjemmel>,
    ): List<String> =
        service
            .findLedigeOppgaverByCriteria(
                criteria = ledigeOppgaverSearchCriteria(selectedHjemler),
                innstillingerHjemmelFilter = InnstillingerHjemmelFilter(allowedHjemler),
            ).searchHits
            .map { it.content.behandlingId }

    private fun ledigeOppgaverSearchCriteria(hjemler: List<Hjemmel>) =
        LedigeOppgaverSearchCriteria(
            ytelser = emptyList(),
            typer = emptyList(),
            hjemler = hjemler,
            offset = 0,
            limit = 10,
            order = no.nav.klage.search.domain.Order.ASC,
            sortField = SortField.FRIST,
            kanBehandleEgenAnsatt = false,
            kanBehandleFortrolig = false,
            kanBehandleStrengtFortrolig = false,
            fristFrom = LocalDate.now().minusDays(3650),
            fristTo = LocalDate.now().plusDays(3650),
            varsletFristFrom = LocalDate.now().minusDays(3650),
            varsletFristTo = LocalDate.now().plusDays(3650),
        )
}
