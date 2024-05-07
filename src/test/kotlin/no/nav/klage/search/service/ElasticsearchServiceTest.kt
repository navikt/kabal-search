package no.nav.klage.search.service

import no.nav.klage.kodeverk.FlowState
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.CountLedigeOppgaverMedUtgaattFristSearchCriteria
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
    fun `lagrer to oppgaver for senere tester`() {

        val klagebehandling1 = EsBehandling(
            behandlingId = "1001L",
            tildeltEnhet = "4219",
            ytelseId = Ytelse.OMS_OMP.id,
            typeId = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2019, 10, 1),
            sakMottattKaDato = LocalDateTime.of(2019, 12, 1, 0, 0),
            frist = LocalDate.of(2020, 12, 1),
            hjemmelIdList = listOf(),
            status = IKKE_TILDELT,
            medunderskriverFlowStateId = FlowState.NOT_SENT.id,
            sakenGjelderFnr = "12345678910",
            fagsystemId = "1",
            rolIdent = "ROLIDENT",
            rolNavn = "ROLNAVN",
            rolFlowStateId = "1",
            saksnummer = "123",
            avsluttetAvSaksbehandler = null,
            returnertFraROL = null,
            tildeltSaksbehandlernavn = null,
            medunderskriverNavn = null,
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
                frist = LocalDate.of(2019, 12, 1),
                hjemmelIdList = listOf(),
                status = IKKE_TILDELT,
                medunderskriverFlowStateId = FlowState.NOT_SENT.id,
                sakenGjelderFnr = "12345678910",
                fagsystemId = "1",
                rolIdent = "ROLIDENT",
                rolNavn = "ROLNAVN",
                rolFlowStateId = "1",
                saksnummer = "123",
                avsluttetAvSaksbehandler = null,
                returnertFraROL = null,
                tildeltSaksbehandlernavn = null,
                medunderskriverNavn = null,
                medunderskriverEnhet = null,
                medunderskriverident = null,
            )
        repo.save(klagebehandling1)
        repo.save(klagebehandling2)

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsBehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(2L)
    }

    @Test
    @Order(4)
    fun `Klagebehandling can be searched for by ytelse`() {
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
                    fristFrom = LocalDate.now().minusDays(3650),
                    fristTo = LocalDate.now().plusDays(3650),
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.first().behandlingId).isEqualTo("1001L")
    }

    @Test
    @Order(5)
    fun `Klagebehandling can be searched for by frist`() {
        val antall =
            service.countLedigeOppgaverMedUtgaattFristByCriteria(
                CountLedigeOppgaverMedUtgaattFristSearchCriteria(
                    typer = emptyList(),
                    ytelser = emptyList(),
                    hjemler = emptyList(),
                    fristFrom = LocalDate.of(2020, 12, 1),
                    fristTo = LocalDate.now(),
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = false,
                )
            )
        assertThat(antall).isEqualTo(1L)
    }

}