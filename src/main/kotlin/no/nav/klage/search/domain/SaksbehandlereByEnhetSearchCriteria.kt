package no.nav.klage.search.domain

data class SaksbehandlereByEnhetSearchCriteria(
    val enhet: String,
    val kanBehandleEgenAnsatt: Boolean,
    val kanBehandleFortrolig: Boolean,
    val kanBehandleStrengtFortrolig: Boolean,
)