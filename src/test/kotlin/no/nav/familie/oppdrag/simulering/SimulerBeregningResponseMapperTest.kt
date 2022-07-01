package no.nav.familie.oppdrag.simulering

import no.nav.familie.oppdrag.simulering.util.lagBeregningStoppniva
import no.nav.familie.oppdrag.simulering.util.lagBeregningStoppnivaFeilUtbetaling
import no.nav.familie.oppdrag.simulering.util.lagBeregningStoppnivaRevurdering
import no.nav.familie.oppdrag.simulering.util.lagBeregningsPeriode
import no.nav.familie.oppdrag.simulering.util.lagSimulerBeregningResponse
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.Month

@ActiveProfiles("dev")
class SimulerBeregningResponseMapperTest() {

    val dagensDato: LocalDate = LocalDate.of(2020, Month.SEPTEMBER, 15)

    @Test
    fun beregn_etterbetaling_føregående_måned() {
        val enTideligereMåned = dagensDato.minusMonths(1)

        val periodeNåværendeMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppniva(dagensDato, 2)), dagensDato
        )

        val periodeTidligereMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppniva(enTideligereMåned)), enTideligereMåned
        )

        val response = lagSimulerBeregningResponse(listOf(periodeNåværendeMåned, periodeTidligereMåned))
        val dto = response.toRestSimulerResult(dagensDato)

        assertEquals(1000, dto.etterbetaling)
    }

    @Test
    fun bergen_etterbetaling_nåværende_og_foregaende_maned() {
        val enTideligereMåned = dagensDato.minusMonths(1)
        val enSenereMåned = dagensDato.plusMonths(1)

        val periodeNesteMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppniva(enSenereMåned)), enSenereMåned
        )

        val periodeNåværendeMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppniva(dagensDato)), dagensDato
        )

        val periodeTidligereMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppniva(enTideligereMåned)), enTideligereMåned
        )

        val response = lagSimulerBeregningResponse(
            listOf(
                periodeNesteMåned,
                periodeNåværendeMåned, periodeTidligereMåned
            )
        )
        val dto = response.toRestSimulerResult(dagensDato)

        assertEquals(2000, dto.etterbetaling)
    }

    @Test
    fun bergen_etterbetaling_med_revurdering() {
        val enTideligereMåned = dagensDato.minusMonths(1)

        val periodeNåværendeMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppniva(dagensDato, 2)), dagensDato
        )

        val periodeTidligereMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppnivaRevurdering(enTideligereMåned)), enTideligereMåned
        )

        val response = lagSimulerBeregningResponse(listOf(periodeNåværendeMåned, periodeTidligereMåned))
        val dto = response.toRestSimulerResult(dagensDato)

        assertEquals(500, dto.etterbetaling)
    }

    @Test
    fun bergen_etterbetaling_med_feilutbetaling() {
        val enTideligereMåned = dagensDato.minusMonths(1)

        val periodeNåværendeMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppniva(dagensDato, 2)), dagensDato
        )

        val periodeTidligereMåned = lagBeregningsPeriode(
            listOf(lagBeregningStoppnivaFeilUtbetaling(enTideligereMåned)), enTideligereMåned
        )

        val response = lagSimulerBeregningResponse(listOf(periodeNåværendeMåned, periodeTidligereMåned))
        val dto = response.toRestSimulerResult(dagensDato)

        assertEquals(0, dto.etterbetaling)
    }

    @Test
    fun bergen_et_år() {
        val beregningsPerioder = mutableListOf<BeregningsPeriode>()

        for (manedNr in 1L..12L) {
            val maned = dagensDato.minusMonths(manedNr)
            beregningsPerioder.add(lagBeregningsPeriode(listOf(lagBeregningStoppniva(maned)), maned))
        }

        val response = lagSimulerBeregningResponse(beregningsPerioder.toList())
        val dto = response.toRestSimulerResult(dagensDato)

        assertEquals(12000, dto.etterbetaling)
    }
}
