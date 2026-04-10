package no.nav.klage.search.clients.azure

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureSlimUser(
    val userPrincipalName: String,
    val onPremisesSamAccountName: String,
    val displayName: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureSlimUserList(val value: List<AzureSlimUser>?)
