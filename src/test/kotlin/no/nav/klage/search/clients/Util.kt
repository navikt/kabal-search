package no.nav.klage.search.clients

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

fun createShortCircuitWebClient(jsonResponse: String): WebClient {
    val clientResponse: ClientResponse = ClientResponse
        .create(HttpStatus.OK)
        .header("Content-Type", "application/json")
        .body(jsonResponse).build()

    val shortCircuitingExchangeFunction = ExchangeFunction {
        Mono.just(clientResponse)
    }

    return WebClient.builder().exchangeFunction(shortCircuitingExchangeFunction).build()
}

fun createShortCircuitWebClientWithStatus(jsonResponse: String, status: HttpStatus): WebClient {
    val clientResponse: ClientResponse = ClientResponse
        .create(status)
        .header("Content-Type", "application/json")
        .body(jsonResponse).build()

    val shortCircuitingExchangeFunction = ExchangeFunction {
        Mono.just(clientResponse)
    }

    return WebClient.builder().exchangeFunction(shortCircuitingExchangeFunction).build()
}