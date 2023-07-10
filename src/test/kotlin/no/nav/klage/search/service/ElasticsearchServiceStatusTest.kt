package no.nav.klage.search.service

import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.config.ElasticsearchServiceConfiguration
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.elasticsearch.EsStatus
import no.nav.klage.search.domain.elasticsearch.EsStatus.*
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
import java.util.*


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
class ElasticsearchServiceStatusTest {

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
    fun `status count works`() {
        repo.save(getKlagebehandling(IKKE_TILDELT))
        repo.save(getKlagebehandling(TILDELT))
        repo.save(getKlagebehandling(MEDUNDERSKRIVER_VALGT))
        repo.save(getKlagebehandling(SENDT_TIL_MEDUNDERSKRIVER))
        repo.save(getKlagebehandling(RETURNERT_TIL_SAKSBEHANDLER))
        repo.save(getKlagebehandling(FULLFOERT))
        repo.save(getKlagebehandling(SATT_PAA_VENT))

        val query = QueryBuilders.matchAllQuery()
        val searchHits: SearchHits<EsBehandling> = repo.search(query)
        assertThat(searchHits.totalHits).isEqualTo(7L)

        val ytelse = Ytelse.OMS_OMP
        val type = Type.KLAGE

        assertThat(service.countIkkeTildelt(ytelse, type)).isEqualTo(1)
        assertThat(service.countTildelt(ytelse, type)).isEqualTo(1)
        assertThat(service.countMedunderskriverValgt(ytelse, type)).isEqualTo(1)
        assertThat(service.countSendtTilMedunderskriver(ytelse, type)).isEqualTo(1)
        assertThat(service.countReturnertTilSaksbehandler(ytelse, type)).isEqualTo(1)
        assertThat(service.countAvsluttet(ytelse, type)).isEqualTo(1)
        assertThat(service.countSattPaaVent(ytelse, type)).isEqualTo(1)
    }

    private fun getKlagebehandling(status: EsStatus) = EsBehandling(
        behandlingId = UUID.randomUUID().toString(),
        tildeltEnhet = "4219",
        ytelseId = Ytelse.OMS_OMP.id,
        typeId = Type.KLAGE.id,
        tildeltSaksbehandlerident = null,
        innsendt = LocalDate.of(2019, 10, 1),
        sakMottattKaDato = LocalDateTime.of(2019, 12, 1, 0, 0),
        frist = LocalDate.of(2020, 12, 1),
        hjemmelIdList = listOf(),
        status = status,
        medunderskriverFlytId = MedunderskriverFlyt.IKKE_SENDT.name,
        sakenGjelderFnr = "12345678910",
        fagsystemId = "1",
        rolIdent = "ROLIDENT",
        rolStateId = "1",
    )

}