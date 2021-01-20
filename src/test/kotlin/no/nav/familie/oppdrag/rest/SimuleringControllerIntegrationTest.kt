package no.nav.familie.oppdrag.rest

import no.nav.familie.oppdrag.repository.SimuleringLagerTjenesteE2E
import no.nav.familie.oppdrag.simulering.SimulerBeregningRequestMapper
import no.nav.familie.oppdrag.simulering.SimuleringTjenesteImpl
import no.nav.familie.oppdrag.simulering.mock.SimuleringSenderMock
import no.nav.familie.oppdrag.simulering.util.lagTestUtbetalingsoppdragForFGBMedEttBarn
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals


@ActiveProfiles("dev")
@SpringBootTest(classes = [SimuleringController::class, SimuleringSenderMock::class, SimuleringTjenesteImpl::class, SimulerBeregningRequestMapper::class, SimuleringLagerTjenesteE2E::class],
                properties = ["spring.cloud.vault.enabled=false"])
internal class SimuleringControllerIntegrasjonTest {

    @Autowired lateinit var simuleringController: SimuleringController

    @Test
    fun test_etterbetalingsbelop() {
        val response = simuleringController.hentEtterbetalingsbeløp(lagTestUtbetalingsoppdragForFGBMedEttBarn())
        assertEquals(3162, response.body?.data?.etterbetaling)
    }
}
