package no.nav.klage.search.clients.pdl.graphql

import no.nav.klage.search.clients.pdl.Beskyttelsesbehov
import no.nav.klage.search.clients.pdl.Person
import no.nav.klage.search.clients.pdl.Sivilstand
import no.nav.klage.search.domain.kodeverk.SivilstandType
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getSecureLogger
import org.springframework.stereotype.Component

@Component
class HentPersonMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapToPerson(fnr: String, pdlPerson: PdlPerson): Person {

        secureLogger.debug("pdl returned {}", pdlPerson)
        return Person(
            foedselsnr = fnr,
            fornavn = pdlPerson.navn.firstOrNull()?.fornavn,
            mellomnavn = pdlPerson.navn.firstOrNull()?.mellomnavn,
            etternavn = pdlPerson.navn.firstOrNull()?.etternavn,
            sammensattNavn = sammensattNavn(pdlPerson.navn.firstOrNull()),
            beskyttelsesbehov = pdlPerson.adressebeskyttelse.firstOrNull()?.gradering?.mapToBeskyttelsesbehov(),
            kjoenn = pdlPerson.kjoenn.firstOrNull()?.kjoenn?.name,
            sivilstand = pdlPerson.sivilstand
                .filter { it.relatertVedSivilstand != null }
                .firstOrNull { it.type == PdlPerson.Sivilstand.SivilstandType.GIFT || it.type == PdlPerson.Sivilstand.SivilstandType.REGISTRERT_PARTNER }
                ?.mapSivilstand()
        )
    }

    private fun PdlPerson.Sivilstand.mapSivilstand(): Sivilstand =
        Sivilstand(this.type.mapType(), this.relatertVedSivilstand!!)

    private fun PdlPerson.Sivilstand.SivilstandType.mapType(): SivilstandType =
        when (this) {
            PdlPerson.Sivilstand.SivilstandType.GIFT -> SivilstandType.GIFT
            PdlPerson.Sivilstand.SivilstandType.REGISTRERT_PARTNER -> SivilstandType.REGISTRERT_PARTNER
            else -> throw IllegalArgumentException("This should never occur")
        }

    fun mapToPersoner(people: List<HentPersonBolkResult>): List<Person> {

        logger.debug("pdl returned {} people", people.size)
        secureLogger.debug("pdl returned {}", people)

        return people.mapNotNull {
            if (it.person == null) null else mapToPerson(it.ident, it.person)
        }
    }

    private fun sammensattNavn(navn: PdlPerson.Navn?): String? =
        navn?.let { "${it.fornavn} ${it.etternavn}" }

    fun PdlPerson.Adressebeskyttelse.GraderingType.mapToBeskyttelsesbehov(): Beskyttelsesbehov? =
        when (this) {
            PdlPerson.Adressebeskyttelse.GraderingType.FORTROLIG -> Beskyttelsesbehov.FORTROLIG
            PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG -> Beskyttelsesbehov.STRENGT_FORTROLIG
            PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG_UTLAND -> Beskyttelsesbehov.STRENGT_FORTROLIG_UTLAND
            else -> null
        }
}
