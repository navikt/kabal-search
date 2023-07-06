package no.nav.klage.search.service

import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.kodeverk.Tema
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
            saksreferanse = "hei"
        )
        repo.save(klagebehandling)

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsBehandling> = repo.search(query)
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
        val searchHits: SearchHits<EsBehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.kildeReferanse).isEqualTo("hallo")
    }

    @Test
    @Order(5)
    fun `print mapping`() {
        val esBehandlingWithAllData = EsBehandling(
            id = "id",
            kildeReferanse = "saksreferanse",
            tildeltEnhet = "abc",
            tema = Tema.OMS.id,
            ytelseId = Ytelse.OMS_OMP.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = "null",
            innsendt = LocalDate.now(),
            mottattFoersteinstans = LocalDate.now(),
            mottattKlageinstans = LocalDateTime.now(),
            frist = LocalDate.now(),
            tildelt = LocalDateTime.now(),
            avsluttet = LocalDateTime.now(),
            hjemler = listOf(Hjemmel.FTRL_8_35.id, Hjemmel.FTRL_8_34.id),
            sakenGjelderFnr = "12345678910",
            sakenGjelderNavn = "Navnet Her",
            egenAnsatt = false,
            fortrolig = false,
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            status = IKKE_TILDELT,
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name,
            sakenGjelderFornavn = "abc",
            sakenGjelderEtternavn = "def",
            sakMottattKaDato = LocalDateTime.now(),
            sakFagsystem = "1",
            sattPaaVent = LocalDate.now(),
            klagerFnr = "null",
            klagerNavn = "null",
            klagerFornavn = "null",
            klagerMellomnavn = "null",
            klagerEtternavn = "null",
            klagerOrgnr = "null",
            klagerOrgnavn = "null",
            sakenGjelderMellomnavn = "null",
            sakenGjelderOrgnr = "null",
            sakenGjelderOrgnavn = "null",
            sakFagsystemNavn = "null",
            sakFagsakId = "null",
            forrigeSaksbehandlerident = "null",
            forrigeBehandlendeEnhet = "null",
            avsenderSaksbehandleridentFoersteinstans = "null",
            avsenderEnhetFoersteinstans = "null",
            forrigeVedtaksDato = LocalDate.now(),
            sendtMedunderskriver = LocalDateTime.now(),
            avsluttetAvSaksbehandler = LocalDateTime.now(),
            tildeltSaksbehandlernavn = "null",
            medunderskriverident = "null",
            medunderskriverNavn = "null",
            hjemlerNavn = listOf("a", "b"),
            saksdokumenter = listOf(EsSaksdokument(journalpostId = "1", dokumentInfoId = "bc")),
            saksdokumenterJournalpostId = listOf("a", "|"),
            saksdokumenterJournalpostIdOgDokumentInfoId = listOf("|", "d"),
            strengtFortrolig = false,
            vedtakUtfall = "null",
            vedtakUtfallNavn = "null",
            vedtakHjemler = listOf("rst", "rsdt"),
            vedtakHjemlerNavn = listOf("rst", "rsdt"),
            sattPaaVentExpires = LocalDate.now(),
            sattPaaVentReason = "null",
            feilregistrert = LocalDateTime.now(),
        )

        repo.save(esBehandlingWithAllData)

        val mappingResponse = client.lowLevelClient.performRequest(Request("GET", "/_mapping"))
        val mapping = EntityUtils.toString(mappingResponse.entity)
        println("mapping: $mapping")

    }

    private fun klagebehandlingWith(id: String, saksreferanse: String): EsBehandling {
        return EsBehandling(
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
            sakenGjelderFnr = "12345678910",
            sakenGjelderNavn = "Navnet Her",
            egenAnsatt = false,
            fortrolig = false,
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            status = IKKE_TILDELT,
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.name,
            sakenGjelderFornavn = "abc",
            sakenGjelderEtternavn = "def",
            sakMottattKaDato = LocalDateTime.now(),
            sakFagsystem = "1",
            sattPaaVent = LocalDate.now(),
        )
    }
}