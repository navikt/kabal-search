package no.nav.klage.search.service.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.mockk
import no.nav.klage.search.clients.klageendret.BehandlingSkjemaV2
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EsBehandlingMapperTest {
    // Same Kotlin/JavaTime setup as BehandlingEndretKafkaConsumer
    private val objectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())

    private val esBehandlingMapper = EsBehandlingMapper(saksbehandlerService = mockk())

    @Test
    fun `behandling med fagsakId mappes til saksnummer`() {
        val behandling = objectMapper.readValue(behandlingJson(sakFagsakId = "\"12345\""), BehandlingSkjemaV2::class.java)

        val esBehandling = esBehandlingMapper.mapBehandlingToEsBehandling(behandling)

        assertThat(esBehandling.saksnummer).isEqualTo("12345")
    }

    @Test
    fun `behandling paa generell sak uten fagsakId gir saksnummer null`() {
        val behandling = objectMapper.readValue(behandlingJson(sakFagsakId = "null"), BehandlingSkjemaV2::class.java)

        val esBehandling = esBehandlingMapper.mapBehandlingToEsBehandling(behandling)

        assertThat(behandling.sakFagsakId).isNull()
        assertThat(esBehandling.saksnummer).isNull()
    }

    private fun behandlingJson(sakFagsakId: String): String =
        """
        {
          "id": "6f1c1c5e-1d0a-4c55-9b0e-000000000001",
          "sakenGjelder": { "person": { "fnr": "12345678910" }, "organisasjon": null },
          "ytelse": { "id": "1", "navn": "ytelse", "beskrivelse": "ytelse" },
          "type": { "id": "1", "navn": "Klage", "beskrivelse": "Klage" },
          "sakFagsystem": { "id": "19", "navn": "FS22", "beskrivelse": "GOSYS" },
          "sakFagsakId": $sakFagsakId,
          "innsendtDato": null,
          "sakMottattKaDato": "2026-10-06T10:52:10",
          "sendtTilTrygderetten": null,
          "ageStartDate": null,
          "avsluttetAvSaksbehandlerTidspunkt": null,
          "returnertFraROLTidspunkt": null,
          "fristDato": null,
          "varsletFristDato": null,
          "gjeldendeTildeling": null,
          "medunderskriver": null,
          "medunderskriverFlowStateId": "0",
          "hjemler": [],
          "vedtak": null,
          "sattPaaVent": null,
          "sattPaaVentExpires": null,
          "sattPaaVentReason": null,
          "sattPaaVentReasonId": null,
          "feilregistrert": null,
          "rolIdent": null,
          "rolFlowStateId": "0",
          "erFortrolig": false,
          "erStrengtFortrolig": false,
          "erEgenAnsatt": false
        }
        """.trimIndent()
}
