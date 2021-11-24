package no.nav.klage.search.repositories

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.search.domain.kodeverk.Ytelse
import no.nav.klage.search.domain.saksbehandler.Enhet
import no.nav.klage.search.gateway.AxsysGateway
import no.nav.klage.search.gateway.AzureGateway
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

internal class SaksbehandlerInfoRepositoryTest {

    private val axsysGateway: AxsysGateway = mockk()
    private val msClient: AzureGateway = mockk()

    private val repo: SaksbehandlerRepository =
        SaksbehandlerRepository(msClient, axsysGateway, "", "", "", "", "", "", "", "")

    @Test
    fun harTilgangTilEnhetOgYtelse() {
        every { axsysGateway.getEnheterForSaksbehandler("01010112345") } returns
                listOf(Enhet("4214", "KA Nord"), Enhet("4219", "KA Sør"))

        val softly = SoftAssertions()
        softly.assertThat(repo.harTilgangTilEnhetOgYtelse("01010112345", "4219", Ytelse.OMS_OMP)).isEqualTo(true)
        softly.assertThat(repo.harTilgangTilEnhetOgYtelse("01010112345", "4290", Ytelse.SYK_SYK)).isEqualTo(false)
        softly.assertThat(repo.harTilgangTilEnhetOgYtelse("01010112345", "4214", Ytelse.OMS_OMP)).isEqualTo(false)
        softly.assertThat(repo.harTilgangTilEnhetOgYtelse("01010112345", "4203", Ytelse.SYK_SYK)).isEqualTo(false)
        softly.assertThat(repo.harTilgangTilEnhetOgYtelse("01010112345", "finnes_ikke", Ytelse.OMS_OMP))
            .isEqualTo(false)
        softly.assertAll()
    }

    @Test
    fun harTilgangTilEnhet() {
        every { axsysGateway.getEnheterForSaksbehandler("01010112345") } returns
                listOf(Enhet("4214", "KA Nord"), Enhet("4219", "KA Sør"))

        val softly = SoftAssertions()
        softly.assertThat(repo.harTilgangTilEnhet("01010112345", "4219")).isEqualTo(true)
        softly.assertThat(repo.harTilgangTilEnhet("01010112345", "4291")).isEqualTo(false)
        softly.assertAll()
    }

    @Test
    fun harTilgangTilYtelse() {
        every { axsysGateway.getEnheterForSaksbehandler("01010112345") } returns
                listOf(Enhet("4219", "KA Sør"))

        val softly = SoftAssertions()
        softly.assertThat(repo.harTilgangTilYtelse("01010112345", Ytelse.SYK_SYK)).isEqualTo(false)
        softly.assertThat(repo.harTilgangTilYtelse("01010112345", Ytelse.OMS_OLP)).isEqualTo(true)
        softly.assertAll()
    }
}