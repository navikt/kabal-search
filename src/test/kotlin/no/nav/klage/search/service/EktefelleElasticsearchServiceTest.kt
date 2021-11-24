package no.nav.klage.search.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.finn.unleash.Unleash
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.KlagebehandlingerSearchCriteria
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
class EktefelleElasticsearchServiceTest {

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

    @Test
    @Order(3)
    fun `lagrer fire oppgaver for senere tester`() {

        val klagebehandling1 = EsKlagebehandling(
            id = "1001L",
            tildeltEnhet = "4219",
            tema = Tema.OMS.id,
            ytelseId = Ytelse.OMS_OMP.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2019, 10, 1),
            mottattFoersteinstans = LocalDate.of(2019, 11, 1),
            mottattKlageinstans = LocalDateTime.of(2019, 12, 1, 0, 0),
            frist = LocalDate.of(2020, 12, 1),
            hjemler = listOf(),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            status = IKKE_TILDELT,
            sakenGjelderFnr = "123",
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
        )
        val klagebehandling2 =
            EsKlagebehandling(
                id = "1002L",
                tildeltEnhet = "4219",
                tema = Tema.SYK.id,
                ytelseId = Ytelse.SYK_SYK.id,
                type = Type.KLAGE.id,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2018, 10, 1),
                mottattFoersteinstans = LocalDate.of(2018, 11, 1),
                mottattKlageinstans = LocalDateTime.of(2018, 12, 1, 0, 0),
                frist = LocalDate.of(2019, 12, 1),
                hjemler = listOf(),
                created = LocalDateTime.now(),
                modified = LocalDateTime.now(),
                kilde = "K9",
                status = IKKE_TILDELT,
                sakenGjelderFnr = "456",
                medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
            )
        val klagebehandling3 = EsKlagebehandling(
            id = "1003L",
            tildeltEnhet = "4219",
            tema = Tema.SYK.id,
            ytelseId = Ytelse.SYK_SYK.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2019, 10, 1),
            mottattFoersteinstans = LocalDate.of(2019, 11, 1),
            mottattKlageinstans = LocalDateTime.of(2019, 12, 1, 0, 0),
            frist = LocalDate.of(2020, 12, 1),
            hjemler = listOf(),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            status = IKKE_TILDELT,
            sakenGjelderFnr = "123",
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
        )
        val klagebehandling4 =
            EsKlagebehandling(
                id = "1004L",
                tildeltEnhet = "4219",
                tema = Tema.OMS.id,
                ytelseId = Ytelse.OMS_OMP.id,
                type = Type.KLAGE.id,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2018, 10, 1),
                mottattFoersteinstans = LocalDate.of(2018, 11, 1),
                mottattKlageinstans = LocalDateTime.of(2018, 12, 1, 0, 0),
                frist = LocalDate.of(2019, 12, 1),
                hjemler = listOf(),
                created = LocalDateTime.now(),
                modified = LocalDateTime.now(),
                kilde = "K9",
                status = IKKE_TILDELT,
                sakenGjelderFnr = "456",
                medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
            )
        repo.save(klagebehandling1)
        repo.save(klagebehandling2)
        repo.save(klagebehandling3)
        repo.save(klagebehandling4)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(4L)
    }

    @Test
    @Order(4)
    fun `Klagebehandling can be searched for by tema`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(2L)
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder("1001L", "1004L")
    }

    @Test
    @Order(5)
    fun `Klagebehandling can be searched for by ytelse`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(2L)
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder("1001L", "1004L")
    }

    @Test
    @Order(6)
    fun `Klagebehandling can be searched for by fnr and tema`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    foedselsnr = "123",
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder("1001L")
    }

    @Test
    @Order(6)
    fun `Klagebehandling can be searched for by fnr and ytelse`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    foedselsnr = "123",
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder("1001L")
    }

    @Test
    @Order(7)
    fun `Klagebehandling can be searched for by ektefelle`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.MOB),
                    foedselsnr = "123",
                    extraPersonAndTema = KlagebehandlingerSearchCriteria.ExtraPersonAndTemaOrYtelse(
                        foedselsnr = "456",
                        temaer = listOf(Tema.SYK),
                        ytelser = emptyList()
                    ),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder("1002L")
    }

    @Test
    @Order(8)
    fun `Klagebehandling can be searched for by ektefelle med ytelse`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.MOB),
                    foedselsnr = "123",
                    extraPersonAndTema = KlagebehandlingerSearchCriteria.ExtraPersonAndTemaOrYtelse(
                        foedselsnr = "456",
                        temaer = emptyList(),
                        ytelser = listOf(Ytelse.SYK_SYK)
                    ),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder("1002L")
    }

    @Test
    @Order(9)
    fun `Klagebehandling can be searched for by fnr and ektefelle`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.SYK),
                    foedselsnr = "123",
                    extraPersonAndTema = KlagebehandlingerSearchCriteria.ExtraPersonAndTemaOrYtelse(
                        foedselsnr = "456",
                        temaer = listOf(Tema.SYK),
                        ytelser = emptyList()
                    ),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(2L)
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder("1002L", "1003L")
    }

    @Test
    @Order(10)
    fun `Klagebehandling can be searched for by fnr and ektefelle med ytelse`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.SYK),
                    foedselsnr = "123",
                    extraPersonAndTema = KlagebehandlingerSearchCriteria.ExtraPersonAndTemaOrYtelse(
                        foedselsnr = "456",
                        temaer = emptyList(),
                        ytelser = listOf(Ytelse.SYK_SYK)
                    ),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(2L)
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder("1002L", "1003L")
    }

}