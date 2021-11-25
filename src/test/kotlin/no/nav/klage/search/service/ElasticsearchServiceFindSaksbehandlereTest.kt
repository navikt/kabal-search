package no.nav.klage.search.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.finn.unleash.Unleash
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.SaksbehandlereByEnhetSearchCriteria
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling.Status.IKKE_TILDELT
import no.nav.klage.search.domain.kodeverk.MedunderskriverFlyt
import no.nav.klage.search.domain.kodeverk.Tema
import no.nav.klage.search.domain.kodeverk.Type
import no.nav.klage.search.domain.kodeverk.Ytelse
import no.nav.klage.search.repositories.EsKlagebehandlingRepository
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
@ImportAutoConfiguration(
    ElasticsearchRestClientAutoConfiguration::class,
    ElasticsearchDataAutoConfiguration::class
)
class ElasticsearchServiceFindSaksbehandlereTest {

    companion object {
        @Container
        @JvmField
        val esContainer: TestElasticsearchContainer = TestElasticsearchContainer.instance
    }

    @MockkBean(relaxed = true)
    lateinit var unleash: Unleash

    @MockkBean(relaxed = true)
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @Autowired
    lateinit var service: ElasticsearchService

    @Autowired
    lateinit var esTemplate: ElasticsearchRestTemplate

    @Autowired
    lateinit var repo: EsKlagebehandlingRepository

    @BeforeEach
    fun setup() {
        every { unleash.isEnabled(any(), false) } returns true
    }

    @Test
    @Order(1)
    fun `es is running`() {
        assertThat(esContainer.isRunning).isTrue
        service.recreateIndex()
    }

    @Test
    @Order(2)
    fun `index has been created by service`() {
        val indexOps = esTemplate.indexOps(IndexCoordinates.of("klagebehandling"))
        assertThat(indexOps.exists()).isTrue
    }

    @Test
    @Order(3)
    fun `lagrer tre oppgaver for senere tester`() {
        repo.save(createEsKlagebehandling(id = "1001", enhet = "4219", saksbehandlerIdent = "Z223", saksbehandlerNavn = "Kalle Bnka", avsluttetAvSaksbehandler = null))
        repo.save(createEsKlagebehandling(id = "1002", enhet = "4219", saksbehandlerIdent = "Z223", saksbehandlerNavn = "Kalle Bnka", avsluttetAvSaksbehandler = null))
        repo.save(createEsKlagebehandling(id = "1003", enhet = "4219", saksbehandlerIdent = "Z123", saksbehandlerNavn = "Kalle Anka", avsluttetAvSaksbehandler = null))
        repo.save(createEsKlagebehandling(id = "1004", enhet = "4219", saksbehandlerIdent = "Z423", saksbehandlerNavn = "Kalle Dnka", avsluttetAvSaksbehandler = null))
        repo.save(createEsKlagebehandling(id = "1005", enhet = "4219", saksbehandlerIdent = "Z323", saksbehandlerNavn = "Kalle Cnka", avsluttetAvSaksbehandler = null))
        repo.save(createEsKlagebehandling(id = "1006", enhet = "4219", saksbehandlerIdent = "Z523", saksbehandlerNavn = "Kalle Enka", avsluttetAvSaksbehandler = LocalDateTime.now()))
        repo.save(createEsKlagebehandling(id = "1007", enhet = "4220", saksbehandlerIdent = "Z623", saksbehandlerNavn = "Kalle Fnka", avsluttetAvSaksbehandler = null))

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(7L)
    }

    @Test
    @Order(4)
    fun `find saksbehandlere based on enhet, but not including finished klagebehandlinger`() {
        val saksbehandlere =
            service.findSaksbehandlereByEnhetCriteria(
                SaksbehandlereByEnhetSearchCriteria(
                    enhetId = "4219"
                )
            )
        assertThat(saksbehandlere.size).isEqualTo(4L)
        assertThat(saksbehandlere.first().second).isEqualTo("Kalle Anka")
        assertThat(saksbehandlere.last().second).isEqualTo("Kalle Dnka")
    }

    private fun createEsKlagebehandling(
        id: String,
        enhet: String,
        saksbehandlerIdent: String,
        saksbehandlerNavn: String,
        avsluttetAvSaksbehandler: LocalDateTime?
    ): EsKlagebehandling {
        return EsKlagebehandling(
            id = id,
            tildeltEnhet = enhet,
            tema = Tema.OMS.id,
            ytelseId = Ytelse.OMS_OMP.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = saksbehandlerIdent,
            tildeltSaksbehandlernavn = saksbehandlerNavn,
            innsendt = LocalDate.of(2019, 10, 1),
            mottattFoersteinstans = LocalDate.of(2019, 11, 1),
            mottattKlageinstans = LocalDateTime.of(2019, 12, 1, 0, 0),
            frist = LocalDate.of(2020, 12, 1),
            hjemler = listOf(),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            status = IKKE_TILDELT,
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name,
            avsluttetAvSaksbehandler = avsluttetAvSaksbehandler
        )
    }

}