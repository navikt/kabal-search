package no.nav.klage.search.domain.kodeverk

enum class Utfall(override val id: String, override val navn: String, override val beskrivelse: String) : Kode {

    TRUKKET("1", "Trukket", "Trukket"),
    RETUR("2", "Retur", "Retur"),
    OPPHEVET("3", "Opphevet", "Opphevet"),
    MEDHOLD("4", "Medhold", "Medhold"),
    DELVIS_MEDHOLD("5", "Delvis medhold", "Delvis medhold"),
    OPPRETTHOLDT("6", "Opprettholdt", "Opprettholdt"),
    UGUNST("7", "Ugunst (Ugyldig)", "Ugunst (Ugyldig)"),
    AVVIST("8", "Avvist", "Avvist");

    override fun toString(): String {
        return "Utfall(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): Utfall {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Utfall with $id exists")
        }
    }

}
