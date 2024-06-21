package no.nav.klage.search.service

import no.nav.klage.kodeverk.FlowState
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria
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
class ElasticsearchServiceFindSaksbehandlereTest {

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
        assertThat(repo.indexExists()).isTrue
    }

    @Test
    @Order(3)
    fun `lagrer oppgaver for senere tester`() {
        repo.save(
            createEsKlagebehandling(
                id = "1001",
                enhet = "4219",
                saksbehandlerIdent = "Z223",
                saksbehandlerNavn = "Kalle Bnka",
                avsluttetAvSaksbehandler = null
            )
        )
        repo.save(
            createEsKlagebehandling(
                id = "1002",
                enhet = "4219",
                saksbehandlerIdent = "Z223",
                saksbehandlerNavn = "Kalle Bnka",
                avsluttetAvSaksbehandler = null
            )
        )
        repo.save(
            createEsKlagebehandling(
                id = "1003",
                enhet = "4219",
                saksbehandlerIdent = "Z123",
                saksbehandlerNavn = "Kalle Anka",
                avsluttetAvSaksbehandler = null
            )
        )
        repo.save(
            createEsKlagebehandling(
                id = "1004",
                enhet = "4219",
                saksbehandlerIdent = "Z423",
                saksbehandlerNavn = "Kalle Dnka",
                avsluttetAvSaksbehandler = null
            )
        )
        repo.save(
            createEsKlagebehandling(
                id = "1005",
                enhet = "4219",
                saksbehandlerIdent = "Z323",
                saksbehandlerNavn = "Kalle Cnka",
                avsluttetAvSaksbehandler = null
            )
        )
        repo.save(
            createEsKlagebehandling(
                id = "1006",
                enhet = "4219",
                saksbehandlerIdent = "Z523",
                saksbehandlerNavn = "Kalle Enka",
                avsluttetAvSaksbehandler = LocalDateTime.now()
            )
        )
        repo.save(
            createEsKlagebehandling(
                id = "1007",
                enhet = "4220",
                saksbehandlerIdent = "Z623",
                saksbehandlerNavn = "Kalle Fnka",
                avsluttetAvSaksbehandler = null
            )
        )

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsBehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(7L)
    }

    @Test
    @Order(4)
    fun `find saksbehandlere based on enhet, but not including finished klagebehandlinger`() {
        val saksbehandlere =
            service.findSaksbehandlereByEnhetCriteria(
                SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria(
                    enhet = "4219",
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = false,
                )
            )
        assertThat(saksbehandlere.size).isEqualTo(4L)
    }

    private fun createEsKlagebehandling(
        id: String,
        enhet: String,
        saksbehandlerIdent: String,
        saksbehandlerNavn: String,
        avsluttetAvSaksbehandler: LocalDateTime?
    ): EsBehandling {
        return EsBehandling(
            behandlingId = id,
            tildeltEnhet = enhet,
            ytelseId = Ytelse.OMS_OMP.id,
            typeId = Type.KLAGE.id,
            tildeltSaksbehandlerident = saksbehandlerIdent,
            tildeltSaksbehandlernavn = saksbehandlerNavn,
            innsendt = LocalDate.of(2019, 10, 1),
            sakMottattKaDato = LocalDateTime.of(2019, 12, 1, 0, 0),
            frist = LocalDate.of(2020, 12, 1),
            varsletFrist = LocalDate.of(2020, 12, 1),
            hjemmelIdList = listOf(),
            status = IKKE_TILDELT,
            medunderskriverFlowStateId = FlowState.NOT_SENT.id,
            avsluttetAvSaksbehandler = avsluttetAvSaksbehandler,
            sakenGjelderFnr = "12345678910",
            fagsystemId = "1",
            rolIdent = "ROLIDENT",
            rolNavn = "ROLNAVN",
            rolFlowStateId = "1",
            saksnummer = "123",
            returnertFraROL = null,
            medunderskriverNavn = null,
            medunderskriverEnhet = null,
            medunderskriverident = null,
        )
    }

}