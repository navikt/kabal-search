package no.nav.klage.search.service

import no.nav.klage.kodeverk.FlowState
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.elasticsearch.EsSaksdokument
import no.nav.klage.search.domain.elasticsearch.EsStatus.IKKE_TILDELT
import no.nav.klage.search.repositories.EsBehandlingRepository
import no.nav.klage.search.repositories.SearchHits
import org.apache.http.util.EntityUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.opensearch.client.Request
import org.opensearch.client.RestHighLevelClient
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
class ElasticsearchIndexingTest {

    companion object {
        @Container
        @JvmField
        val esContainer: TestOpenSearchContainer = TestOpenSearchContainer.instance
    }

    @Autowired
    lateinit var service: ElasticsearchService

    @Autowired
    lateinit var repo: EsBehandlingRepository

    @Autowired
    lateinit var client: RestHighLevelClient

    @Test
    @Order(1)
    fun `es is running`() {
        assertThat(esContainer.isRunning).isTrue
    }

    @Test
    @Order(2)
    fun `index has been created by service`() {
        assertThat(repo.indexExists()).isTrue
        service.recreateIndex()
    }

    @Test
    @Order(3)
    fun `klagebehandling can be saved and retrieved`() {

        val klagebehandling = klagebehandlingWith(
            id = "1001L",
        )
        repo.save(klagebehandling)

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsBehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.behandlingId).isEqualTo("1001L")
    }

    @Test
    @Order(4)
    fun `klagebehandling can be saved twice without creating a duplicate`() {

        var klagebehandling = klagebehandlingWith(
            id = "2001L",
        )
        repo.save(klagebehandling)

        klagebehandling = klagebehandlingWith(
            id = "2001L",
        )
        repo.save(klagebehandling)

        val query = QueryBuilders.idsQuery().addIds("2001L")
        val searchHits: SearchHits<EsBehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(1L)
    }

    @Test
    @Order(5)
    fun `print mapping`() {
        val esBehandlingWithAllData = EsBehandling(
            behandlingId = "id",
            tildeltEnhet = "abc",
            ytelseId = Ytelse.OMS_OMP.id,
            typeId = Type.KLAGE.id,
            tildeltSaksbehandlerident = "null",
            innsendt = LocalDate.now(),
            sakMottattKaDato = LocalDateTime.now(),
            frist = LocalDate.now(),
            hjemmelIdList = listOf(Hjemmel.FTRL_8_35.id, Hjemmel.FTRL_8_34.id),
            sakenGjelderFnr = "12345678910",
            egenAnsatt = false,
            fortrolig = false,
            status = IKKE_TILDELT,
            medunderskriverNavn = null,
            medunderskriverEnhet = null,
            medunderskriverFlowStateId = FlowState.NOT_SENT.id,
            fagsystemId = "1",
            sattPaaVent = LocalDate.now(),
            avsluttetAvSaksbehandler = LocalDateTime.now(),
            returnertFraROL = null,
            tildeltSaksbehandlernavn = "null",
            medunderskriverident = "null",
            saksdokumenter = listOf(EsSaksdokument(journalpostId = "1", dokumentInfoId = "bc")),
            strengtFortrolig = false,
            utfallId = "null",
            sattPaaVentExpires = LocalDate.now(),
            sattPaaVentReason = "null",
            feilregistrert = LocalDateTime.now(),
            rolIdent = "null",
            rolNavn = null,
            rolFlowStateId = FlowState.NOT_SENT.id,
            saksnummer = "123",
        )

        repo.save(esBehandlingWithAllData)

        val mappingResponse = client.lowLevelClient.performRequest(Request("GET", "/_mapping"))
        val mapping = EntityUtils.toString(mappingResponse.entity)
        println("mapping: $mapping")

    }

    private fun klagebehandlingWith(id: String): EsBehandling {
        return EsBehandling(
            behandlingId = id,
            tildeltEnhet = "",
            ytelseId = Ytelse.OMS_OMP.id,
            typeId = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = null,
            sakMottattKaDato = LocalDateTime.now(),
            frist = null,
            hjemmelIdList = listOf(Hjemmel.FTRL_8_35.id),
            sakenGjelderFnr = "12345678910",
            egenAnsatt = false,
            fortrolig = false,
            status = IKKE_TILDELT,
            medunderskriverFlowStateId = FlowState.NOT_SENT.id,
            fagsystemId = "1",
            sattPaaVent = LocalDate.now(),
            rolIdent = "ROLIDENT",
            rolFlowStateId = FlowState.NOT_SENT.id,
            saksnummer = "123",
            avsluttetAvSaksbehandler = null,
            returnertFraROL = null,
            tildeltSaksbehandlernavn = null,
            medunderskriverident = null,
            medunderskriverNavn = null,
            medunderskriverEnhet = null,
            rolNavn = null,
        )
    }
}