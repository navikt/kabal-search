package no.nav.klage.search.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.finn.unleash.Unleash
import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.kodeverk.Tema
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling.Status.IKKE_TILDELT
import no.nav.klage.search.repositories.EsKlagebehandlingRepository
import no.nav.klage.search.repositories.SearchHits
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.*
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

    @MockkBean(relaxed = true)
    lateinit var unleash: Unleash

    @Autowired
    lateinit var service: ElasticsearchService

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
        assertThat(repo.indexExists()).isTrue()
    }

    @Test
    @Order(3)
    fun `lagrer to oppgaver for senere tester`() {

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
                medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
            )
        repo.save(klagebehandling1)
        repo.save(klagebehandling2)

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsKlagebehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(2L)
    }

    @Test
    @Order(4)
    fun `Klagebehandling can be searched for by ytelse`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    ytelser = listOf(Ytelse.OMS_OMP),
                    offset = 0,
                    limit = 10,
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = false,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.first().id).isEqualTo("1001L")
    }

    @Test
    @Order(5)
    fun `Klagebehandling can be searched for by frist`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    fristFom = LocalDate.of(2020, 12, 1),
                    offset = 0,
                    limit = 10,
                    kanBehandleEgenAnsatt = false,
                    kanBehandleFortrolig = false,
                    kanBehandleStrengtFortrolig = false,
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.first().id).isEqualTo("1001L")
    }

}