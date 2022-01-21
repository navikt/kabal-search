package no.nav.klage.search.service

import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
@Disabled
class RelatedKlagebehandlingerTest {
    /*

    companion object {
        @Container
        @JvmField
        val esContainer: TestOpenSearchContainer = TestOpenSearchContainer.instance
    }

    @Autowired
    lateinit var service: ElasticsearchService

    @Autowired
    lateinit var repo: EsKlagebehandlingRepository

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

    private fun klagebehandling(
        id: Long,
        fnr: String,
        saksreferanse: String?,
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
            klagebehandling(1001L, "01019012345", "AAA123", true),
            klagebehandling(1002L, "02019012345", "AAA123", false),
            klagebehandling(1003L, "03019012345", "BBB123", true),
            klagebehandling(1004L, "01019012345", "BBB123", false),
            klagebehandling(1005L, "02019012345", "CCC123", true),
            klagebehandling(1006L, "03019012345", "CCC123", false)
        )
        repo.saveAll(klagebehandlinger)

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsKlagebehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(6L)
    }

    @Test
    @Order(4)
    fun `related klagebehandlinger gives correct answer`() {
        val related = service.findRelatedKlagebehandlinger("01019012345", "AAA123")
        val softly = SoftAssertions()
        softly.assertThat(related.aapneByFnr.map { it.id }).containsExactly("1001")
        softly.assertThat(related.avsluttedeByFnr.map { it.id }).containsExactly("1004")
        softly.assertThat(related.aapneBySaksreferanse.map { it.id }).containsExactly("1001")
        softly.assertThat(related.avsluttedeBySaksreferanse.map { it.id }).containsExactly("1002")
        softly.assertAll()
    }

    @Test
    @Order(5)
    fun `format date works`() {

        val searchCriteria = KlagebehandlingerSearchCriteria(
            ferdigstiltFom = LocalDate.now(),
            offset = 0,
            limit = 10,
            statuskategori = Statuskategori.AVSLUTTET,
            kanBehandleEgenAnsatt = false,
            kanBehandleFortrolig = false,
            kanBehandleStrengtFortrolig = false,
        )

        val results = service.findByCriteria(searchCriteria)
        assertThat(results.searchHits).hasSize(3)
    }

     */

}