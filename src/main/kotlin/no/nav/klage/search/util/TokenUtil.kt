package no.nav.klage.search.util

import no.nav.klage.auth.TexasClient
import no.nav.klage.auth.currentAzureToken
import no.nav.klage.auth.navIdent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TokenUtil(
    private val texasClient: TexasClient,
    @Value($$"api://${NAIS_CLUSTER_NAME}.${NAIS_NAMESPACE}.${KABAL_INNSTILLINGER_APP_NAME}/.default")
    private val kabalInnstillingerScope: String,
    @Value($$"api://${NAIS_CLUSTER_NAME}.${NAIS_NAMESPACE}.${KLAGE_LOOKUP_APP_NAME}/.default")
    private val klageLookupScope: String,
) {
    companion object {
        const val GRAPH_SCOPE = "https://graph.microsoft.com/.default"
    }

    fun getUserAccessTokenWithKabalInnstillingerScope(): String = texasClient.onBehalfOfCurrentUser(kabalInnstillingerScope)

    fun getAppAccessTokenWithGraphScope(): String = texasClient.machineToMachineToken(GRAPH_SCOPE)

    fun getSaksbehandlerAccessTokenWithKlageLookupScope(): String = texasClient.onBehalfOfCurrentUser(klageLookupScope)

    fun getAppAccessTokenWithKlageLookupScope(): String = texasClient.machineToMachineToken(klageLookupScope)

    fun getIdent(): String = currentAzureToken()?.navIdent ?: throw RuntimeException("Ident not found in token")
}
