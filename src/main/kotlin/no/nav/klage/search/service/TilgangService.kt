package no.nav.klage.search.service

import no.nav.klage.search.clients.egenansatt.EgenAnsattService
import no.nav.klage.search.clients.pdl.PdlFacade
import no.nav.klage.search.domain.kodeverk.Tema
import no.nav.klage.search.exceptions.MissingTilgangException
import no.nav.klage.search.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.search.repositories.SaksbehandlerRepository
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerRepository: SaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun verifyInnloggetSaksbehandlersTilgangTil(fnr: String) {
        if (!harInnloggetSaksbehandlerTilgangTil(fnr)) {
            throw MissingTilgangException("Saksbehandler har ikke tilgang til denne brukeren")
        }
    }

    fun verifyInnloggetSaksbehandlersTilgangTilEnhet(enhetId: String) {
        //TODO: Burde man sjekket tilgang til EnhetOgTema, ikke bare enhet?
        if (!innloggetSaksbehandlerRepository.harTilgangTilEnhet(enhetId)) {
            throw MissingTilgangException("Saksbehandler har ikke tilgang til enhet $enhetId")
        }
    }

    fun verifySaksbehandlersTilgangTilEnhetOgTema(saksbehandlerIdent: String, enhetId: String, tema: Tema) {
        if (!saksbehandlerRepository.harTilgangTilEnhetOgTema(saksbehandlerIdent, enhetId, tema)) {
            throw MissingTilgangException("Saksbehandler har ikke tilgang til tema $tema i enhet $enhetId")
        }
    }

    fun verifyInnloggetSaksbehandlersTilgangTilTema(tema: Tema) {
        if (!innloggetSaksbehandlerRepository.harTilgangTilTema(tema)) {
            throw MissingTilgangException("Saksbehandler har ikke tilgang til tema $tema")
        }
    }

    fun harInnloggetSaksbehandlerTilgangTil(fnr: String): Boolean {
        val ident = innloggetSaksbehandlerRepository.getInnloggetIdent()
        return verifiserTilgangTilPersonForSaksbehandler(
            fnr = fnr,
            ident = ident,
            kanBehandleStrengtFortrolig = { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() },
            kanBehandleFortrolig = { innloggetSaksbehandlerRepository.kanBehandleFortrolig() },
            kanBehandleEgenAnsatt = { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() },
        )
    }

    fun harSaksbehandlerTilgangTil(ident: String, fnr: String): Boolean {
        return verifiserTilgangTilPersonForSaksbehandler(
            fnr = fnr,
            ident = ident,
            kanBehandleStrengtFortrolig = { saksbehandlerRepository.kanBehandleStrengtFortrolig(ident) },
            kanBehandleFortrolig = { saksbehandlerRepository.kanBehandleFortrolig(ident) },
            kanBehandleEgenAnsatt = { saksbehandlerRepository.kanBehandleEgenAnsatt(ident) },
        )
    }

    private fun verifiserTilgangTilPersonForSaksbehandler(
        fnr: String,
        ident: String,
        kanBehandleStrengtFortrolig: () -> Boolean,
        kanBehandleFortrolig: () -> Boolean,
        kanBehandleEgenAnsatt: () -> Boolean
    ): Boolean {
        val personInfo = pdlFacade.getPersonInfo(fnr)
        val harBeskyttelsesbehovFortrolig = personInfo.harBeskyttelsesbehovFortrolig()
        val harBeskyttelsesbehovStrengtFortrolig = personInfo.harBeskyttelsesbehovStrengtFortrolig()
        val erEgenAnsatt = egenAnsattService.erEgenAnsatt(fnr)

        if (harBeskyttelsesbehovStrengtFortrolig) {
            securelogger.info("erStrengtFortrolig")
            //Merk at vi ikke sjekker egenAnsatt her, strengt fortrolig trumfer det
            if (kanBehandleStrengtFortrolig.invoke()) {
                securelogger.info("Access granted to strengt fortrolig for $ident")
            } else {
                securelogger.info("Access denied to strengt fortrolig for $ident")
                return false
            }
        }
        if (harBeskyttelsesbehovFortrolig) {
            securelogger.info("erFortrolig")
            //Merk at vi ikke sjekker egenAnsatt her, fortrolig trumfer det
            if (kanBehandleFortrolig.invoke()) {
                securelogger.info("Access granted to fortrolig for $ident")
            } else {
                securelogger.info("Access denied to fortrolig for $ident")
                return false
            }
        }
        if (erEgenAnsatt && !(harBeskyttelsesbehovFortrolig || harBeskyttelsesbehovStrengtFortrolig)) {
            securelogger.info("erEgenAnsatt")
            //Er kun egenAnsatt, har ikke et beskyttelsesbehov i tillegg
            if (kanBehandleEgenAnsatt.invoke()) {
                securelogger.info("Access granted to egen ansatt for $ident")
            } else {
                securelogger.info("Access denied to egen ansatt for $ident")
                return false
            }
        }
        return true
    }
}
