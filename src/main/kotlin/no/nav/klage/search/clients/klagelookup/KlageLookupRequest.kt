package no.nav.klage.search.clients.klagelookup

data class BatchedUserRequest(
    val navIdentList: List<String>
)