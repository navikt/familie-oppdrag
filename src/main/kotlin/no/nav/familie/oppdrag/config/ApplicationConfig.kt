package no.nav.familie.oppdrag.config

import no.nav.familie.log.filter.LogFilter
import no.nav.familie.log.filter.RequestTimeFilter
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootConfiguration
@EntityScan(ApplicationConfig.pakkenavn, "no.nav.familie.sikkerhet")
@ComponentScan(ApplicationConfig.pakkenavn, "no.nav.familie.sikkerhet")
@EnableScheduling
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
class ApplicationConfig {

    @Bean
    fun servletWebServerFactory(): ServletWebServerFactory {
        val serverFactory = JettyServletWebServerFactory()
        serverFactory.port = 8087
        return serverFactory
    }

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        val filterRegistration = FilterRegistrationBean<LogFilter>()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    fun requestTimeFilter(): FilterRegistrationBean<RequestTimeFilter> {
        val filterRegistration = FilterRegistrationBean<RequestTimeFilter>()
        filterRegistration.filter = RequestTimeFilter()
        filterRegistration.order = 2
        return filterRegistration
    }

    companion object {
        const val pakkenavn = "no.nav.familie.oppdrag"
        val LOKALE_PROFILER = setOf("dev", "e2e", "dev_psql_mq")
    }
}
