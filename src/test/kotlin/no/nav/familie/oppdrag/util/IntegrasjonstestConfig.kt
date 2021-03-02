package no.nav.familie.oppdrag.util

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Profile("integrasjonstest")
@Import(TokenGeneratorConfiguration::class)
@Configuration
class IntegrasjonstestConfig