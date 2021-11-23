package no.nav.klage.search.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.finn.unleash.Unleash
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling.Status.UKJENT
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
class RelatedKlagebehandlingerTest {

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
        assertThat(indexOps.exists()).isTrue()
    }

    private fun klagebehandling(
        id: Long,
        fnr: String,
        saksreferanse: String?,
        journalpostIder: List<String>,
        aapen: Boolean
    ) =
        EsKlagebehandling(
            id = id.toString(),
            tildeltEnhet = "4219",
            tema = Tema.OMS.id,
            ytelseId = Ytelse.OMS_OMP.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.now(),
            mottattFoersteinstans = LocalDate.now(),
            mottattKlageinstans = LocalDateTime.now(),
            frist = LocalDate.now(),
            avsluttet = if (aapen) {
                null
            } else {
                LocalDateTime.now()
            },
            avsluttetAvSaksbehandler = if (aapen) {
                null
            } else {
                LocalDateTime.now()
            },
            hjemler = listOf(),
            sakenGjelderFnr = fnr,
            kildeReferanse = saksreferanse,
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            status = UKJENT,
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
        )

    @Test
    @Order(3)
    fun `saving klagebehandlinger for later tests`() {

        val klagebehandlinger = listOf(
            klagebehandling(1001L, "01019012345", "AAA123", listOf(), true),
            klagebehandling(1002L, "02019012345", "AAA123", listOf(), false),
            klagebehandling(1003L, "03019012345", "BBB123", listOf(), true),
            klagebehandling(1004L, "01019012345", "BBB123", listOf(), false),
            klagebehandling(1005L, "02019012345", "CCC123", listOf("111222", "333444"), true),
            klagebehandling(1006L, "03019012345", "CCC123", listOf("333444", "555666"), false)
        )
        repo.saveAll(klagebehandlinger)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(6L)
    }

    @Test
    @Order(4)
    fun `related klagebehandlinger gives correct answer`() {
        val related = service.findRelatedKlagebehandlinger("01019012345", "AAA123", listOf("333444", "777888"))
        assertThat(related.aapneByFnr.map { it.id }).containsExactly("1001")
        assertThat(related.avsluttedeByFnr.map { it.id }).containsExactly("1004")
        assertThat(related.aapneBySaksreferanse.map { it.id }).containsExactly("1001")
        assertThat(related.avsluttedeBySaksreferanse.map { it.id }).containsExactly("1002")
        assertThat(related.aapneByJournalpostid.map { it.id }).containsExactly("1005")
        assertThat(related.avsluttedeByJournalpostid.map { it.id }).containsExactly("1006")
    }

    @Test
    @Order(5)
    fun `format date works`() {

        val searchCriteria = KlagebehandlingerSearchCriteria(
            ferdigstiltFom = LocalDate.now(),
            offset = 0,
            limit = 10,
            statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.AVSLUTTET
        )

        val results = service.findByCriteria(searchCriteria)
        assertThat(results.searchHits).hasSize(3)
    }

}