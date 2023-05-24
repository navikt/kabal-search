package no.nav.klage.search.clients.pdl

import no.nav.klage.search.domain.kodeverk.SivilstandType

data class Person(
    val foedselsnr: String,
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val sammensattNavn: String?,
    val beskyttelsesbehov: Beskyttelsesbehov?,
    val kjoenn: String?,
    val sivilstand: Sivilstand?
) {
    fun harBeskyttelsesbehovFortrolig() = beskyttelsesbehov == Beskyttelsesbehov.FORTROLIG

    fun harBeskyttelsesbehovStrengtFortrolig() =
        beskyttelsesbehov == Beskyttelsesbehov.STRENGT_FORTROLIG || beskyttelsesbehov == Beskyttelsesbehov.STRENGT_FORTROLIG_UTLAND

}

data class Sivilstand(val type: SivilstandType, val foedselsnr: String)

enum class Beskyttelsesbehov {
    STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG, FORTROLIG
}
