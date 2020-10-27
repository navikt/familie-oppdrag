package no.nav.familie.oppdrag.rest

import no.nav.familie.oppdrag.simulering.SimulerBeregningRequestMapper
import no.nav.familie.oppdrag.simulering.SimuleringSender
import no.nav.familie.oppdrag.simulering.SimuleringTjenesteImpl
import no.nav.familie.oppdrag.simulering.util.ClientMocks
import no.nav.familie.oppdrag.simulering.util.lagTestUtbetalingsoppdragForFGBMedEttBarn
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals


@ActiveProfiles("dev")
@SpringBootTest(classes = [SimuleringController::class, SimuleringSender::class, SimuleringTjenesteImpl::class, SimulerBeregningRequestMapper::class, ClientMocks::class],
                properties = ["spring.cloud.vault.enabled=false"])
internal class SimuleringControllerIntegrasjonTest {

    @Autowired lateinit var simuleringController: SimuleringController

    @Test
    fun test_etterbetalingsbelop() {
        val response = simuleringController.hentEtterbetalingsbeløp(lagTestUtbetalingsoppdragForFGBMedEttBarn())
        assertEquals(1000, response.body?.data?.etterbetaling)
    }
}