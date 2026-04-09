package no.nav.klage.search.clients.klagelookup

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.klage.kodeverk.AzureGroup

data class UsersResponse(
    val users: List<UserResponse>,
)

data class UserResponse(
    val navIdent: String,
    val sammensattNavn: String,
    val fornavn: String,
    val etternavn: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExtendedUsersResponse(
    val hits: List<ExtendedUserResponse>,
    val misses: List<String>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExtendedUserResponse(
    val navIdent: String,
    val sammensattNavn: String,
    val fornavn: String,
    val etternavn: String,
    val enhet: Enhet,
)

data class Enhet(
    val enhetNr: String,
    val enhetNavn: String,
)

data class GroupsResponse(
    val groupIds: List<String>,
)

data class SaksbehandlerGroups(
    val groups: List<AzureGroup>
)