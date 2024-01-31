package no.nav.familie.oppdrag.simulering.util

import io.mockk.every
import io.mockk.mockk
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerFpService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
class ClientMocks {
    @Bean
    @Profile("dev")
    @Primary
    fun mockSimulerFpService(): SimulerFpService {
        val simulerFpService = mockk<SimulerFpService>()

        every {
            simulerFpService.simulerBeregning(any())
        } answers {
            lagTestSimuleringResponse()
        }

        return simulerFpService
    }
}
