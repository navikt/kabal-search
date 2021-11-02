package no.nav.klage.search.domain.kodeverk

enum class RaadfoertMedLege(override val id: String, override val navn: String, override val beskrivelse: String) :
    Kode {

    MANGLER("1", "Mangler", "Saken burde vært forelagt for ROL i vedtaksinstansen"),
    RIKTIG(
        "2",
        "Riktig",
        "Saken er godt nok medisinsk opplyst med ROL-uttalelse i vedtaksinstansen/uten at ROL har blitt konsultert"
    ),
    MANGELFULL(
        "3",
        "Mangelfull",
        "Saken er forelagt ROL i vedtaksinstans, men er fortsatt mangelfullt medisinsk vurdert"
    ),
    UAKTUELT("4", "Uaktuelt", "Saken handler ikke om trygdemedisinske vurderinger");

    override fun toString(): String {
        return "Rol(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): RaadfoertMedLege {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No RaadfoertMedLege with $id exists")
        }
    }

}

