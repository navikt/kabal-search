package no.nav.klage.search.domain.saksbehandler

import no.nav.klage.search.domain.kodeverk.Tema

data class EnhetMedLovligeTemaer(val enhetId: String, val navn: String, val temaer: List<Tema>)