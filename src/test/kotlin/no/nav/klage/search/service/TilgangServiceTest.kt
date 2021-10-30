package no.nav.klage.search.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.search.clients.egenansatt.EgenAnsattService
import no.nav.klage.search.clients.pdl.Beskyttelsesbehov
import no.nav.klage.search.clients.pdl.PdlFacade
import no.nav.klage.search.clients.pdl.Person
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.repositories.SaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TilgangServiceTest {

    private val pdlFacade: PdlFacade = mockk()

    private val egenAnsattService: EgenAnsattService = mockk()

    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository = mockk()

    private val saksbehandlerRepository: SaksbehandlerRepository = mockk()

    private val tilgangService =
        TilgangService(pdlFacade, egenAnsattService, innloggetSaksbehandlerRepository, saksbehandlerRepository)
       

    @Test
    fun `harSaksbehandlerTilgangTil gir false på fortrolig`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.FORTROLIG,
                kjoenn = "",
                sivilstand = null
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() }.returns(false)
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() }.returns(false)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på strengt fortrolig`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.STRENGT_FORTROLIG,
                kjoenn = "",
                sivilstand = null
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() }.returns(false)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på egen ansatt`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = null,
                kjoenn = "",
                sivilstand = null
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() }.returns(false)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(true)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir true på egen ansatt når saksbehandler har egenAnsatt rettigheter`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = null,
                kjoenn = "",
                sivilstand = null
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() }.returns(true)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(true)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(true)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir true på fortrolig når saksbehandler har fortrolig rettigheter`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.FORTROLIG,
                kjoenn = "",
                sivilstand = null
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() }.returns(false)
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() }.returns(true)
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() }.returns(false)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(true)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på fortrolig når saksbehandler har strengt fortrolig rettigheter`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.FORTROLIG,
                kjoenn = "",
                sivilstand = null
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() }.returns(false)
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() }.returns(false)
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() }.returns(true)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir true på fortrolig kombinert med egen ansatt når saksbehandler har fortrolig rettigheter men ikke egen ansatt`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.FORTROLIG,
                kjoenn = "",
                sivilstand = null
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() }.returns(false)
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() }.returns(true)
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() }.returns(false)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(true)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(true)
    }
}
