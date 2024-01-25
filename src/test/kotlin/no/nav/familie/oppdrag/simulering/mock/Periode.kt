package no.nav.familie.oppdrag.simulering.mock

import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate

data class Periode constructor(
    val fom: LocalDate,
    var tom: LocalDate,
    val sats: BigDecimal?,
    val oldSats: BigDecimal?,
    val typeSats: String?,
    var periodeType: PeriodeType?,
    val kodeKlassifik: String?,
) : Comparable<Periode> {
    constructor(
        fom: LocalDate,
        tom: LocalDate,
        sats: BigDecimal? = null,
        typeSats: String? = null,
        kodeKlassifik: String? = null,
        periodeType: PeriodeType? = null,
    ) :
        this(
            fom = fom,
            tom = tom,
            sats = sats,
            oldSats = null,
            typeSats = typeSats,
            kodeKlassifik = kodeKlassifik,
            periodeType = periodeType,
        )

    constructor(
        fom: LocalDate,
        tom: LocalDate,
        oldSats: BigDecimal,
        sats: BigDecimal?,
        typeSats: String?,
        kodeKlassifik: String?,
    ) :
        this(
            fom = fom,
            tom = tom,
            oldSats = oldSats,
            sats = sats,
            typeSats = typeSats,
            kodeKlassifik = kodeKlassifik,
            periodeType =
                if (oldSats <= sats) {
                    PeriodeType.ØKNING
                } else {
                    PeriodeType.REDUKSJON
                },
        )

    override fun compareTo(other: Periode): Int {
        return fom.compareTo(other.fom)
    }

    val antallVirkedager: Int
        get() {
            var startDato = fom
            var antallVirkedager = 0
            while (startDato.isBefore(tom) || startDato.isEqual(tom)) {
                if (startDato.dayOfWeek != DayOfWeek.SATURDAY && startDato.dayOfWeek != DayOfWeek.SUNDAY) {
                    antallVirkedager++
                }
                startDato = startDato.plusDays(1)
            }
            return antallVirkedager
        }
}
