package no.nav.familie.oppdrag.tss

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

internal class TssOppslagServiceTest {

    private val mqClient: TssMQClient = mockk()

    lateinit var service: TssOppslagService

    @BeforeEach
    fun setUp() {
        service = TssOppslagService(mqClient)
    }

    @Test
    fun b910() {
        val slot = slot<String>()
        every { mqClient.kallTss(capture(slot)) } returns "OK"
        service.hentInformasjonOmSamhandler("8003424215")

        assertNotNull(slot.captured)
    }
}
