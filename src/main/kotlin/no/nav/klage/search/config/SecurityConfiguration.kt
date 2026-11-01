package no.nav.klage.search.config

import no.nav.klage.auth.AzureAuthProperties
import no.nav.klage.auth.azureJwtDecoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfiguration {
    companion object {
        // Swagger is only enabled in dev (application-dev.yaml). In prod these paths give 404.
        val UNAUTHENTICATED_PATHS =
            listOf(
                "/internal/health",
                "/internal/health/**",
                "/internal/info",
                "/internal/prometheus",
                "/error",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/aapenfeaturetoggle/*",
            )
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        environment: Environment,
    ): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(*UNAUTHENTICATED_PATHS.toTypedArray()).permitAll()
                // DevOnlyAdminController, used by the e2e tests. Only opened when that controller exists.
                if (environment.acceptsProfiles(Profiles.of("dev"))) {
                    it.requestMatchers(HttpMethod.GET, "/internal/elasticadmin/nuke").permitAll()
                    it.requestMatchers(HttpMethod.DELETE, "/internal/behandlinger/*").permitAll()
                }
                it.anyRequest().authenticated()
            }.oauth2ResourceServer { it.jwt { } }
            .build()

    @Bean
    fun jwtDecoder(environment: Environment): JwtDecoder = azureJwtDecoder(AzureAuthProperties.from(environment))
}
