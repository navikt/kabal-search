package no.nav.klage.search.domain.kodeverk

import io.swagger.annotations.ApiModel
import org.springframework.core.env.Environment
import java.util.*

@ApiModel
enum class Type(override val id: String, override val navn: String, override val beskrivelse: String) : Kode {

    KLAGE("1", "Klage", "Klage"),
    ANKE("2", "Anke", "Anke")
    ;

    override fun toString(): String {
        return "Type(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): Type {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Type with $id exists")
        }

        fun fromNavn(navn: String): Type {
            return values().firstOrNull { it.navn == navn }
                ?: throw IllegalArgumentException("No Type with $navn exists")
        }
    }
}

object LovligeTyper {
    private val lovligeTyperIProdGcp = EnumSet.of(Type.KLAGE)
    private val lovligeTyperIDevGcp = EnumSet.of(Type.KLAGE)

    fun lovligeTyper(environment: Environment): EnumSet<Type> = if (environment.activeProfiles.contains("prod-gcp")) {
        lovligeTyperIProdGcp
    } else {
        lovligeTyperIDevGcp
    }
}
