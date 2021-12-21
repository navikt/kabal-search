package no.nav.klage.search.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.finn.unleash.Unleash
import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.kodeverk.Tema
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling.Status.*
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
class ElasticsearchServiceStatusTest {

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
        assertThat(repo.indexExists()).isTrue
    }

    @Test
    @Order(3)
    fun `status count works`() {
        repo.save(getKlagebehandling(IKKE_TILDELT))
        repo.save(getKlagebehandling(TILDELT))
        repo.save(getKlagebehandling(MEDUNDERSKRIVER_VALGT))
        repo.save(getKlagebehandling(SENDT_TIL_MEDUNDERSKRIVER))
        repo.save(getKlagebehandling(RETURNERT_TIL_SAKSBEHANDLER))
        repo.save(getKlagebehandling(FULLFOERT))

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsKlagebehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(6L)

        assertThat(service.countIkkeTildelt()).isEqualTo(1)
        assertThat(service.countTildelt()).isEqualTo(1)
        assertThat(service.countMedunderskriverValgt()).isEqualTo(1)
        assertThat(service.countSendtTilMedunderskriver()).isEqualTo(1)
        assertThat(service.countReturnertTilSaksbehandler()).isEqualTo(1)
        assertThat(service.countAvsluttet()).isEqualTo(1)
    }

    private fun getKlagebehandling(status: EsKlagebehandling.Status) = EsKlagebehandling(
        id = UUID.randomUUID().toString(),
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
        status = status,
        medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name
    )

}