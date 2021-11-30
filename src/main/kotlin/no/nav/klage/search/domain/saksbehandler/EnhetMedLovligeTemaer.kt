package no.nav.klage.search.domain.saksbehandler

import no.nav.klage.kodeverk.Tema

data class EnhetMedLovligeTemaer(val enhetId: String, val navn: String, val temaer: List<Tema>)