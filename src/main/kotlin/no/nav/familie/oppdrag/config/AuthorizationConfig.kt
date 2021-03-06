package no.nav.familie.oppdrag.config

import no.nav.familie.oppdrag.config.ApplicationConfig.Companion.LOKALE_PROFILER
import no.nav.familie.sikkerhet.AuthorizationFilter
import no.nav.familie.sikkerhet.OIDCUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class AuthorizationConfig(
        private val oidcUtil: OIDCUtil,
        @Value("\${ACCEPTED_CLIENTS}")
        private val acceptedClients: List<String>,
        private val environment: Environment
) {

    @Bean
    fun authorizationFilter(): AuthorizationFilter {
        return AuthorizationFilter(oidcUtil = oidcUtil,
                                   acceptedClients = acceptedClients,
                                   disabled = environment.activeProfiles.any {
                                       LOKALE_PROFILER.contains(it.trim(' '))
                                   })
    }
}
