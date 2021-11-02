package no.nav.klage.search.domain.kodeverk

enum class UtsendingStatus(override val id: String, override val navn: String, override val beskrivelse: String) :
    Kode {
    IKKE_SENDT("1", "Ikke sendt", "Ikke sendt"),
    FEILET("2", "Feilet", "Feilet"),
    SENDT("3", "Sendt", "Sendt");

    companion object {
        fun of(id: String): UtsendingStatus {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No UtsendingStatus with $id exists")
        }
    }
}
