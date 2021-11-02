package no.nav.klage.search.domain.kodeverk

import io.swagger.annotations.ApiModel

@ApiModel
enum class MedunderskriverFlyt(override val id: String, override val navn: String, override val beskrivelse: String) :
    Kode {
    IKKE_SENDT("1", "IKKE_SENDT", "Ikke sendt til medunderskriver"),
    OVERSENDT_TIL_MEDUNDERSKRIVER("2", "OVERSENDT_TIL_MEDUNDERSKRIVER", "Oversendt til medunderskriver"),
    RETURNERT_TIL_SAKSBEHANDLER("3", "RETURNERT_TIL_SAKSBEHANDLER", "Returnert til saksbehandler")
    ;

    override fun toString(): String {
        return "Medunderskriverflyt(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): MedunderskriverFlyt {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Medunderskriverflyt with $id exists")
        }

        fun fromNavn(navn: String?): MedunderskriverFlyt {
            return values().firstOrNull { it.navn == navn }
                ?: throw IllegalArgumentException("No Medunderskriverflyt with $navn exists")
        }
    }
}