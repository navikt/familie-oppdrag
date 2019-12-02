package no.nav.familie.oppdrag

import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.jms.core.JmsTemplate

@Configuration
@Profile("dev")
class OppdragMQTestConfig {

    @Bean
    @Primary
    fun jmsMockTemplate(): JmsTemplate {
        
        // TODO: Bytt ut dette med et lokalt docker-oppsett for MQ
        return mockk()
    }
}