package no.nav.klage.search.domain.elasticsearch

data class KlageStatistikk(
    val ubehandlede: Long,
    val overFrist: Long,
    val innsendtIGaar: Long,
    val innsendtSiste7Dager: Long,
    val innsendtSiste30Dager: Long,
    val behandletIGaar: Long,
    val behandletSiste7Dager: Long,
    val behandletSiste30Dager: Long
)