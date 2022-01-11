package no.nav.klage.search.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.finn.unleash.Unleash
import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.kodeverk.Tema
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling.Status.IKKE_TILDELT
import no.nav.klage.search.repositories.EsKlagebehandlingRepository
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.repositories.SearchHits
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
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

    @MockkBean(relaxed = true)
    lateinit var unleash: Unleash

    @MockkBean(relaxed = true)
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

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
            saksreferanse = "hei"
        )
        repo.save(klagebehandling)

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsKlagebehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.kildeReferanse).isEqualTo("hei")
    }

    @Test
    @Order(4)
    fun `klagebehandling can be saved twice without creating a duplicate`() {

        var klagebehandling = klagebehandlingWith(
            id = "2001L",
            saksreferanse = "hei"
        )
        repo.save(klagebehandling)

        klagebehandling = klagebehandlingWith(
            id = "2001L",
            saksreferanse = "hallo"
        )
        repo.save(klagebehandling)

        val query = QueryBuilders.idsQuery().addIds("2001L")
        val searchHits: SearchHits<EsKlagebehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.kildeReferanse).isEqualTo("hallo")
    }

    private fun klagebehandlingWith(id: String, saksreferanse: String): EsKlagebehandling {
        return EsKlagebehandling(
            id = id,
            kildeReferanse = saksreferanse,
            tildeltEnhet = "",
            tema = Tema.OMS.id,
            ytelseId = Ytelse.OMS_OMP.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = null,
            mottattFoersteinstans = null,
            mottattKlageinstans = LocalDateTime.now(),
            frist = null,
            tildelt = null,
            avsluttet = null,
            hjemler = listOf(Hjemmel.FTRL_8_35.id),
            sakenGjelderFnr = null,
            sakenGjelderNavn = null,
            egenAnsatt = false,
            fortrolig = false,
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            status = IKKE_TILDELT,
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
        )
    }
}