package no.nav.klage.search.clients.pdl.graphql

import no.nav.klage.search.clients.pdl.Beskyttelsesbehov
import no.nav.klage.search.clients.pdl.Person
import no.nav.klage.search.util.getLogger
import org.springframework.stereotype.Component

@Component
class HentPersonMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun mapToPerson(fnr: String, pdlPerson: PdlPerson): Person {
        return Person(
            foedselsnr = fnr,
            beskyttelsesbehov = pdlPerson.adressebeskyttelse.firstOrNull()?.gradering?.mapToBeskyttelsesbehov(),
        )
    }

    fun PdlPerson.Adressebeskyttelse.GraderingType.mapToBeskyttelsesbehov(): Beskyttelsesbehov? =
        when (this) {
            PdlPerson.Adressebeskyttelse.GraderingType.FORTROLIG -> Beskyttelsesbehov.FORTROLIG
            PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG -> Beskyttelsesbehov.STRENGT_FORTROLIG
            PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG_UTLAND -> Beskyttelsesbehov.STRENGT_FORTROLIG_UTLAND
            else -> null
        }
}
