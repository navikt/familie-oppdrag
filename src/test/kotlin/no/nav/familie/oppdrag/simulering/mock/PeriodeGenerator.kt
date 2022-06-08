package no.nav.familie.oppdrag.simulering.mock

import no.nav.system.os.entiteter.typer.simpletypes.KodeStatusLinje
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdragslinje
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections

class PeriodeGenerator {
    var opphørsPerioder: MutableList<Periode> = ArrayList()
    var ytelsesPerioder: MutableList<Periode> = ArrayList()
    fun genererPerioder(oppdragslinjeList: List<Oppdragslinje>): List<Periode>? {
        lagOpphørOgYtelse(oppdragslinjeList)
        Collections.sort(ytelsesPerioder)
        Collections.sort(opphørsPerioder)
        if (opphørsPerioder.isEmpty() && ytelsesPerioder.isEmpty()) {
            return null
        }
        if (opphørsPerioder.isEmpty()) {
            for (periode in ytelsesPerioder) {
                periode.periodeType = PeriodeType.YTEL
            }
            return ytelsesPerioder
        }
        if (ytelsesPerioder.isEmpty()) {
            for (periode in opphørsPerioder) {
                periode.periodeType = PeriodeType.OPPH
            }
            return opphørsPerioder
        }
        val perioder: MutableList<Periode> = ArrayList(definerYtelseOgOmpostPerioder())
        perioder.addAll(definerOpphørPerioder())
        Collections.sort(perioder)
        return perioder
    }

    private fun definerOpphørPerioder(): List<Periode> {
        val periodeList: MutableList<Periode> = ArrayList()
        val opphørPerioderTemp: MutableList<Periode> = ArrayList(opphørsPerioder)
        val removeOpphørPerioder: MutableList<Periode> = ArrayList()
        val addOpphørPerioder: MutableList<Periode> = ArrayList()
        while (!opphørPerioderTemp.isEmpty()) {
            opphørloop@ for (opphør in opphørPerioderTemp) {
                // ytelseloop:
                for (ytelse in ytelsesPerioder) {
                    // Scenario 1
                    if (opphør.fom.isBefore(ytelse.fom) && opphør.tom.isBefore(ytelse.fom)) {
                        periodeList.add(
                            Periode(
                                opphør.fom,
                                opphør.tom,
                                opphør.sats,
                                opphør.typeSats,
                                opphør.kodeKlassifik,
                                PeriodeType.OPPH
                            )
                        )
                        removeOpphørPerioder.add(opphør)
                        continue@opphørloop
                        // Scenario 2
                    } else if (opphør.fom.isBefore(ytelse.fom) && opphør.tom.isEqual(ytelse.fom)) {
                        periodeList.add(
                            Periode(
                                opphør.fom,
                                opphør.tom.minusDays(1),
                                opphør.sats,
                                opphør.typeSats,
                                opphør.kodeKlassifik,
                                PeriodeType.OPPH
                            )
                        )
                        removeOpphørPerioder.add(opphør)
                        continue@opphørloop
                        // Scenario 3,4 og 5
                    } else if (opphør.fom.isBefore(ytelse.fom) && opphør.tom.isAfter(ytelse.fom)) {
                        periodeList.add(
                            Periode(
                                opphør.fom,
                                ytelse.fom.minusDays(1),
                                opphør.sats,
                                opphør.typeSats,
                                opphør.kodeKlassifik,
                                PeriodeType.OPPH
                            )
                        )
                        removeOpphørPerioder.add(opphør)
                        // Scenario 3 & 4
                        if (!opphør.tom.isAfter(ytelse.tom)) {
                            continue@opphørloop
                        } else { // if opphør.getTom().isAfter(ytelse.getTom())
                            addOpphørPerioder.add(
                                Periode(
                                    ytelse.tom.plusDays(1),
                                    opphør.tom,
                                    opphør.sats,
                                    opphør.typeSats,
                                    opphør.kodeKlassifik
                                )
                            )
                            continue@opphørloop
                        }
                    } else if (!opphør.fom.isBefore(ytelse.fom) && !opphør.tom.isAfter(ytelse.tom)) {
                        removeOpphørPerioder.add(opphør)
                        continue@opphørloop
                    } else if (opphør.fom.isEqual(ytelse.fom) && opphør.tom.isAfter(ytelse.tom)) {
                        removeOpphørPerioder.add(opphør)
                        addOpphørPerioder.add(
                            Periode(
                                ytelse.tom.plusDays(1),
                                opphør.tom,
                                opphør.sats,
                                opphør.typeSats,
                                opphør.kodeKlassifik
                            )
                        )
                        continue@opphørloop
                    } else if (opphør.fom.isAfter(ytelse.fom) && !opphør.fom.isAfter(ytelse.tom)) {
                        removeOpphørPerioder.add(opphør)
                        addOpphørPerioder.add(
                            Periode(
                                ytelse.tom.plusDays(1),
                                opphør.tom,
                                opphør.sats,
                                opphør.typeSats,
                                opphør.kodeKlassifik
                            )
                        )
                        continue@opphørloop
                    }
                    // Scenario 12 - sjekkes mot neste ytelsesperiode
                }
                // Scenario 12 - hvis den ikke treffer noen ytelsesperioder.
                periodeList.add(
                    Periode(
                        opphør.fom,
                        opphør.tom,
                        opphør.sats,
                        opphør.typeSats,
                        opphør.kodeKlassifik,
                        PeriodeType.OPPH
                    )
                )
                removeOpphørPerioder.add(opphør)
            }
            opphørPerioderTemp.removeAll(removeOpphørPerioder)
            removeOpphørPerioder.clear()
            opphørPerioderTemp.addAll(addOpphørPerioder)
            addOpphørPerioder.clear()
            Collections.sort(opphørPerioderTemp)
        }
        Collections.sort(periodeList)
        return periodeList
    }

    private fun definerYtelseOgOmpostPerioder(): List<Periode> {
        val periodeList: MutableList<Periode> = ArrayList()
        val ytelsesPerioderTemp: MutableList<Periode> = ArrayList(ytelsesPerioder)
        val removeYtelsesPerioder: MutableList<Periode> = ArrayList()
        val addYtelsesPerioder: MutableList<Periode> = ArrayList()
        while (!ytelsesPerioderTemp.isEmpty()) {
            ytelseloop@ for (ytelse in ytelsesPerioderTemp) {
                // opphørloop:
                for (opphør in opphørsPerioder) {
                    // Scenarioer beskrevet i readme.md - Ytelse sjekkes mot opphør
                    // Scenario 1
                    if (ytelse.fom.isBefore(opphør.fom) && ytelse.tom.isBefore(opphør.fom)) {
                        periodeList.add(
                            Periode(
                                ytelse.fom,
                                ytelse.tom,
                                ytelse.sats,
                                ytelse.typeSats,
                                ytelse.kodeKlassifik,
                                PeriodeType.YTEL
                            )
                        )
                        removeYtelsesPerioder.add(ytelse)
                        continue@ytelseloop
                        // Scenario 2
                    } else if (ytelse.fom.isBefore(opphør.fom) && ytelse.tom.isEqual(opphør.fom)) {
                        periodeList.add(
                            Periode(
                                ytelse.fom,
                                ytelse.tom.minusDays(1),
                                ytelse.sats,
                                ytelse.typeSats,
                                ytelse.kodeKlassifik,
                                PeriodeType.YTEL
                            )
                        )
                        opphør.sats?.let {
                            Periode(
                                ytelse.tom, ytelse.tom,
                                it, ytelse.sats, ytelse.typeSats, ytelse.kodeKlassifik
                            )
                        }?.let { periodeList.add(it) }
                        removeYtelsesPerioder.add(ytelse)
                        continue@ytelseloop
                        // Scenario 3,4 og 5
                    } else if (ytelse.fom.isBefore(opphør.fom) && ytelse.tom.isAfter(opphør.fom)) {
                        periodeList.add(
                            Periode(
                                ytelse.fom,
                                opphør.fom.minusDays(1),
                                ytelse.sats,
                                ytelse.typeSats,
                                ytelse.kodeKlassifik,
                                PeriodeType.YTEL
                            )
                        )
                        // Scenario 3 & 4
                        if (!ytelse.tom.isAfter(opphør.tom)) {
                            opphør.sats?.let {
                                Periode(
                                    opphør.fom,
                                    ytelse.tom,
                                    it,
                                    ytelse.sats,
                                    ytelse.typeSats,
                                    ytelse.kodeKlassifik
                                )
                            }?.let {
                                periodeList.add(
                                    it
                                )
                            }
                            removeYtelsesPerioder.add(ytelse)
                            continue@ytelseloop
                            // Scenario 5
                        } else { // if (ytelse.getTom().isAfter(opphør.getTom())
                            opphør.sats?.let {
                                Periode(
                                    opphør.fom,
                                    opphør.tom,
                                    it,
                                    ytelse.sats,
                                    ytelse.typeSats,
                                    ytelse.kodeKlassifik
                                )
                            }?.let {
                                periodeList.add(
                                    it
                                )
                            }
                            addYtelsesPerioder.add(
                                Periode(
                                    opphør.tom.plusDays(1),
                                    ytelse.fom,
                                    ytelse.sats,
                                    ytelse.typeSats,
                                    ytelse.kodeKlassifik
                                )
                            ) // Nytt objekt på slutten til samme loop
                            removeYtelsesPerioder.add(ytelse)
                            continue@ytelseloop
                        }
                        // Scenario 6 & 7
                    } else if (ytelse.fom.isEqual(opphør.fom) && !ytelse.tom.isAfter(opphør.tom)) {
                        opphør.sats?.let {
                            Periode(
                                ytelse.fom, ytelse.tom,
                                it, ytelse.sats, ytelse.typeSats, ytelse.kodeKlassifik
                            )
                        }?.let { periodeList.add(it) }
                        removeYtelsesPerioder.add(ytelse)
                        continue@ytelseloop
                        // Scenario 8
                    } else if (ytelse.fom.isEqual(opphør.fom) && ytelse.tom.isAfter(opphør.tom)) {
                        opphør.sats?.let {
                            Periode(
                                ytelse.fom, opphør.tom,
                                it, ytelse.sats, ytelse.typeSats, ytelse.kodeKlassifik
                            )
                        }?.let { periodeList.add(it) }
                        addYtelsesPerioder.add(
                            Periode(
                                opphør.tom.plusDays(1),
                                ytelse.tom,
                                ytelse.sats,
                                ytelse.typeSats,
                                ytelse.kodeKlassifik
                            )
                        ) // //Nytt objekt på slutten til samme loop
                        removeYtelsesPerioder.add(ytelse)
                        continue@ytelseloop
                        // Scenario 9
                    } else if (ytelse.fom.isAfter(opphør.fom) && !ytelse.tom.isAfter(opphør.tom)) { // ytelse.getFom() er implisit før opphør.getTom() da ytelse.getFom() ikke kan være etter ytelse.getTom()
                        opphør.sats?.let {
                            Periode(
                                ytelse.fom, ytelse.tom,
                                it, ytelse.sats, ytelse.typeSats, ytelse.kodeKlassifik
                            )
                        }?.let { periodeList.add(it) }
                        removeYtelsesPerioder.add(ytelse)
                        continue@ytelseloop
                        // Scenario 10 & 11
                    } else if (ytelse.fom.isAfter(opphør.fom) && !ytelse.fom.isAfter(opphør.tom)) { // ytelse.getTom() er implisit after opphør.getTom() ellers ville den truffet forrige if-statement
                        opphør.sats?.let {
                            Periode(
                                ytelse.fom, opphør.tom,
                                it, ytelse.sats, ytelse.typeSats, ytelse.kodeKlassifik
                            )
                        }?.let { periodeList.add(it) }
                        addYtelsesPerioder.add(
                            Periode(
                                opphør.tom.plusDays(1),
                                ytelse.tom,
                                ytelse.sats,
                                ytelse.typeSats,
                                ytelse.kodeKlassifik
                            )
                        )
                        removeYtelsesPerioder.add(ytelse)
                        continue@ytelseloop
                    }
                    // Scenario 12 - prøver igjen mot neste opphørsperiode
                }
                // Scenario 12 - hvis ytelsen ikke treffer noen opphørsperioder.
                periodeList.add(
                    Periode(
                        ytelse.fom,
                        ytelse.tom,
                        ytelse.sats,
                        ytelse.typeSats,
                        ytelse.kodeKlassifik,
                        PeriodeType.YTEL
                    )
                ) // Hvis ytelsen ikke treffer noen opphør
                removeYtelsesPerioder.add(ytelse)
            }
            ytelsesPerioderTemp.removeAll(removeYtelsesPerioder)
            removeYtelsesPerioder.clear()
            ytelsesPerioderTemp.addAll(addYtelsesPerioder)
            addYtelsesPerioder.clear()
            Collections.sort(ytelsesPerioderTemp)
        }
        return periodeList
    }

    private fun lagOpphørOgYtelse(oppdragslinjeList: List<Oppdragslinje>) {
        for (oppdragslinje in oppdragslinjeList) {
            assert(oppdragslinje.typeSats == "MND" || oppdragslinje.typeSats == "DAG") { "Forventet at typeSats er enten DAG eller MND, men typeSats var: " + oppdragslinje.typeSats }
            if (oppdragslinje.kodeEndringLinje == "ENDR") {
                assert(oppdragslinje.kodeStatusLinje == KodeStatusLinje.OPPH) { "Forventet at KodeStatusLinje er OPPH når KodeEndringLinje er ENDR" }
                if (oppdragslinje.datoStatusFom != null) {
                    opphørsPerioder.add(
                        Periode(
                            LocalDate.parse(oppdragslinje.datoStatusFom, dateTimeFormatter),
                            LocalDate.parse(oppdragslinje.datoVedtakTom, dateTimeFormatter),
                            oppdragslinje.sats,
                            oppdragslinje.typeSats,
                            oppdragslinje.kodeKlassifik
                        )
                    )
                } else {
                    opphørsPerioder.add(
                        Periode(
                            LocalDate.parse(oppdragslinje.datoVedtakFom, dateTimeFormatter),
                            LocalDate.parse(oppdragslinje.datoVedtakTom, dateTimeFormatter),
                            oppdragslinje.sats,
                            oppdragslinje.typeSats,
                            oppdragslinje.kodeKlassifik
                        )
                    )
                }
            } else if (oppdragslinje.kodeEndringLinje == "NY") {
                ytelsesPerioder.add(
                    Periode(
                        LocalDate.parse(oppdragslinje.datoVedtakFom, dateTimeFormatter),
                        LocalDate.parse(oppdragslinje.datoVedtakTom, dateTimeFormatter),
                        oppdragslinje.sats,
                        oppdragslinje.typeSats,
                        oppdragslinje.kodeKlassifik
                    )
                )
            } else {
                throw IllegalArgumentException("Forventet kodeEndringLinje NY eller ENDR. Verdi var: " + oppdragslinje.kodeEndringLinje)
            }
        }
    }

    companion object {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
