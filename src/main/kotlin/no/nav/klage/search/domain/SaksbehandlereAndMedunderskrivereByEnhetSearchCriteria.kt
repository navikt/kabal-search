package no.nav.klage.search.domain

data class SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria(
    val enhet: String,
    override val kanBehandleEgenAnsatt: Boolean,
    override val kanBehandleFortrolig: Boolean,
    override val kanBehandleStrengtFortrolig: Boolean,
) : SecuritySearchCriteria