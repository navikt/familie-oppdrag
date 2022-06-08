package no.nav.familie.oppdrag.simulering.mock

import no.nav.system.os.entiteter.beregningskjema.Beregning
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaa
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaaDetaljer
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdrag
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdragslinje
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.ArrayList

class SimuleringGenerator {
    var periodeGenerator = PeriodeGenerator()
    var erRefusjon: Boolean? = null
    var refunderesOrgNr: String? = null
    var oppdragsPeriodeList: List<Periode>? = ArrayList()
    fun opprettSimuleringsResultat(simulerBeregningRequest: SimulerBeregningRequest): SimulerBeregningResponse {
        erRefusjon = erRefusjon(simulerBeregningRequest.request.oppdrag.oppdragslinje)
        oppdragsPeriodeList = periodeGenerator.genererPerioder(simulerBeregningRequest.request.oppdrag.oppdragslinje)
        val response = SimulerBeregningResponse()
        val beregning = lagBeregning(simulerBeregningRequest)
        if (beregning == null) {
            response.response = null
        } else {
            val innerResponse =
                no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningResponse()
            response.response = innerResponse
            innerResponse.simulering = beregning
        }
        return response
    }

    private fun erRefusjon(oppdragslinjer: List<Oppdragslinje>): Boolean {
        if (oppdragslinjer.isEmpty() || oppdragslinjer[0].refusjonsInfo == null) {
            return false
        }
        refunderesOrgNr = oppdragslinjer[0].refusjonsInfo.refunderesId
        for (oppdragslinje in oppdragslinjer) {
            require(!(oppdragslinje.refusjonsInfo.refunderesId.isEmpty() || oppdragslinje.refusjonsInfo.refunderesId != refunderesOrgNr)) { "Ved refusjon må alle oppdragslinjer ha samme refusjonsInfo. Både orgnr " + refunderesOrgNr + " og " + oppdragslinje.refusjonsInfo.refunderesId + "ble funnet i samme request." }
        }
        return true
    }

    private fun lagBeregning(simulerBeregningRequest: SimulerBeregningRequest): Beregning? {
        val beregning = Beregning()
        leggTilBeregningsperioder(simulerBeregningRequest, beregning)
        if (beregning.beregningsPeriode.size == 0) {
            return null
        }
        beregning.gjelderId = simulerBeregningRequest.request.oppdrag.oppdragGjelderId
        beregning.gjelderNavn = "DUMMY"
        beregning.datoBeregnet = dateTimeFormatter.format(LocalDate.now())
        beregning.kodeFaggruppe = "KORTTID"
        beregning.belop = BigDecimal.valueOf(1234L)
        return beregning
    }

    private fun leggTilBeregningsperioder(simulerBeregningRequest: SimulerBeregningRequest, beregning: Beregning) {
        val nesteMåned: YearMonth
        nesteMåned = if (LocalDate.now().dayOfMonth <= 19) {
            YearMonth.from(LocalDate.now())
        } else {
            YearMonth.from(LocalDate.now().plusMonths(1))
        }
        val beregningsPerioder = beregning.beregningsPeriode
        for (oppdragsperiode in oppdragsPeriodeList!!) {
            var sisteMåned: YearMonth?
            sisteMåned = if (oppdragsperiode.periodeType == PeriodeType.OPPH) {
                nesteMåned.minusMonths(1)
            } else {
                nesteMåned
            }
            if (!YearMonth.from(oppdragsperiode.fom).isAfter(sisteMåned) && oppdragsperiode.antallVirkedager != 0) {
                while (YearMonth.from(oppdragsperiode.tom).isAfter(sisteMåned)) {
                    oppdragsperiode.tom = oppdragsperiode.tom.minusMonths(1)
                        .withDayOfMonth(oppdragsperiode.tom.minusMonths(1).lengthOfMonth())
                }
                val beregningsPeriode =
                    opprettBeregningsperiode(oppdragsperiode, simulerBeregningRequest.request.oppdrag)
                if (!beregningsPeriode.beregningStoppnivaa.isEmpty()) {
                    beregningsPerioder.add(
                        opprettBeregningsperiode(
                            oppdragsperiode,
                            simulerBeregningRequest.request.oppdrag
                        )
                    )
                }
            }
        }
    }

    private fun opprettBeregningsperiode(oppdragsperiode: Periode, oppdrag: Oppdrag): BeregningsPeriode {
        val beregningsPeriode = BeregningsPeriode()
        beregningsPeriode.periodeFom = oppdragsperiode.fom.format(dateTimeFormatter)
        beregningsPeriode.periodeTom = oppdragsperiode.tom.format(dateTimeFormatter)
        beregningsPeriode.beregningStoppnivaa.addAll(opprettBeregningStoppNivaa(oppdragsperiode, oppdrag))
        return beregningsPeriode
    }

    private fun opprettBeregningStoppNivaa(oppdragsperiode: Periode, oppdrag: Oppdrag): List<BeregningStoppnivaa> {
        val perioder = splittOppIPeriodePerMnd(oppdragsperiode)
        val beregningStoppnivaaer: MutableList<BeregningStoppnivaa> = ArrayList()
        val nesteMåned: YearMonth
        val sisteMåned: YearMonth
        nesteMåned = if (LocalDate.now().dayOfMonth <= 19) {
            YearMonth.from(LocalDate.now())
        } else {
            YearMonth.from(LocalDate.now().plusMonths(1))
        }
        sisteMåned = if (oppdragsperiode.periodeType == PeriodeType.OPPH) {
            nesteMåned.minusMonths(1)
        } else {
            nesteMåned
        }
        for (periode in perioder) {
            if (!YearMonth.from(periode.fom).isAfter(sisteMåned)) {
                val stoppnivaa = BeregningStoppnivaa()
                stoppnivaa.kodeFagomraade = oppdrag.kodeFagomraade
                if (erRefusjon!!) {
                    stoppnivaa.utbetalesTilId = refunderesOrgNr
                    stoppnivaa.utbetalesTilNavn = "DUMMY FIRMA"
                } else {
                    stoppnivaa.utbetalesTilId = oppdrag.oppdragGjelderId
                    stoppnivaa.utbetalesTilNavn = "DUMMY"
                }
                stoppnivaa.behandlendeEnhet = "8052"
                var forfallsdato = LocalDate.now()
                if (YearMonth.from(periode.fom) == nesteMåned) {
                    forfallsdato = LocalDate.now().withDayOfMonth(20)
                }
                stoppnivaa.forfall = dateTimeFormatter.format(forfallsdato)
                stoppnivaa.oppdragsId = 1234L
                stoppnivaa.stoppNivaaId = BigInteger.ONE
                stoppnivaa.fagsystemId = oppdrag.fagsystemId
                stoppnivaa.bilagsType = "U"
                stoppnivaa.isFeilkonto = oppdragsperiode.periodeType == PeriodeType.OPPH
                stoppnivaa.kid = "12345"
                if (oppdragsperiode.periodeType == PeriodeType.OPPH) {
                    for (i in 1..3) {
                        stoppnivaa.beregningStoppnivaaDetaljer.add(
                            opprettNegativBeregningStoppNivaaDetaljer(
                                periode,
                                oppdragsperiode,
                                i
                            )
                        )
                    }
                } else if (oppdragsperiode.periodeType == PeriodeType.REDUKSJON && YearMonth.from(periode.fom)
                    .isBefore(nesteMåned)
                ) {
                    stoppnivaa.beregningStoppnivaaDetaljer.add(
                        opprettBeregningStoppNivaaDetaljer(
                            periode,
                            oppdragsperiode
                        )
                    )
                    for (i in 2..3) {
                        stoppnivaa.beregningStoppnivaaDetaljer.add(
                            opprettNegativBeregningStoppNivaaDetaljer(
                                periode,
                                oppdragsperiode,
                                i
                            )
                        )
                    }
                } else if (oppdragsperiode.periodeType == PeriodeType.ØKNING && YearMonth.from(periode.fom)
                    .isBefore(nesteMåned)
                ) {
                    stoppnivaa.beregningStoppnivaaDetaljer.add(
                        opprettNegativBeregningStoppNivaaDetaljer(
                            periode,
                            oppdragsperiode,
                            3
                        )
                    )
                    stoppnivaa.beregningStoppnivaaDetaljer.add(
                        opprettBeregningStoppNivaaDetaljer(
                            periode,
                            oppdragsperiode
                        )
                    )
                } else if (oppdragsperiode.periodeType != PeriodeType.OPPH) {
                    stoppnivaa.beregningStoppnivaaDetaljer.add(
                        opprettBeregningStoppNivaaDetaljer(
                            periode,
                            oppdragsperiode
                        )
                    )
                }
                if (!stoppnivaa.beregningStoppnivaaDetaljer.isEmpty()) {
                    beregningStoppnivaaer.add(stoppnivaa)
                }
            }
        }
        return beregningStoppnivaaer
    }

    private fun opprettBeregningStoppNivaaDetaljer(
        periode: Periode,
        oppdragsperiode: Periode
    ): BeregningStoppnivaaDetaljer {
        val stoppnivaaDetaljer = BeregningStoppnivaaDetaljer()
        stoppnivaaDetaljer.faktiskFom = dateTimeFormatter.format(periode.fom)
        stoppnivaaDetaljer.faktiskTom = dateTimeFormatter.format(periode.tom)
        stoppnivaaDetaljer.kontoStreng = "1235432"
        stoppnivaaDetaljer.behandlingskode = "2"
        if (periode.typeSats == "DAG") {
            stoppnivaaDetaljer.belop = oppdragsperiode.sats!!.multiply(BigDecimal.valueOf(periode.antallVirkedager.toLong()))
        } else stoppnivaaDetaljer.belop = oppdragsperiode.sats
        stoppnivaaDetaljer.trekkVedtakId = 0L
        stoppnivaaDetaljer.stonadId = "1234"
        stoppnivaaDetaljer.korrigering = ""
        stoppnivaaDetaljer.isTilbakeforing = false
        stoppnivaaDetaljer.linjeId = BigInteger.valueOf(21423L)
        stoppnivaaDetaljer.sats = oppdragsperiode.sats
        stoppnivaaDetaljer.typeSats = periode.typeSats
        stoppnivaaDetaljer.antallSats = BigDecimal.valueOf(periode.antallVirkedager.toLong())
        stoppnivaaDetaljer.saksbehId = "5323"
        stoppnivaaDetaljer.uforeGrad = BigInteger.valueOf(100L)
        stoppnivaaDetaljer.kravhaverId = ""
        stoppnivaaDetaljer.delytelseId = "3523"
        stoppnivaaDetaljer.bostedsenhet = "4643"
        stoppnivaaDetaljer.skykldnerId = ""
        stoppnivaaDetaljer.klassekode = oppdragsperiode.kodeKlassifik
        stoppnivaaDetaljer.klasseKodeBeskrivelse = "DUMMY"
        stoppnivaaDetaljer.typeKlasse = "YTEL"
        stoppnivaaDetaljer.typeKlasseBeskrivelse = "DUMMY"
        if (erRefusjon!! && refunderesOrgNr != null) {
            stoppnivaaDetaljer.refunderesOrgNr = refunderesOrgNr
        } else stoppnivaaDetaljer.refunderesOrgNr = ""
        return stoppnivaaDetaljer
    }

    private fun opprettNegativBeregningStoppNivaaDetaljer(
        periode: Periode,
        oppdragsperiode: Periode,
        sequence: Int
    ): BeregningStoppnivaaDetaljer {
        val stoppnivaaDetaljer = BeregningStoppnivaaDetaljer()

        // Sequence explanation:
        // 1.Ytelsen slik den stod original
        // 2.Feilutbetalt beløp
        // 3.Fjerning av ytelsen fra seqence 1
        stoppnivaaDetaljer.faktiskFom = dateTimeFormatter.format(periode.fom)
        stoppnivaaDetaljer.faktiskTom = dateTimeFormatter.format(periode.tom)
        stoppnivaaDetaljer.kontoStreng = "1235432"
        if (sequence == 2) {
            stoppnivaaDetaljer.behandlingskode = "0"
        } else {
            stoppnivaaDetaljer.behandlingskode = "2"
        }
        stoppnivaaDetaljer.belop = setBeløp(periode.antallVirkedager, oppdragsperiode, sequence)
        stoppnivaaDetaljer.trekkVedtakId = 0L
        stoppnivaaDetaljer.stonadId = ""
        if (sequence == 2) {
            stoppnivaaDetaljer.korrigering = "J"
        } else {
            stoppnivaaDetaljer.korrigering = ""
        }
        stoppnivaaDetaljer.isTilbakeforing = sequence == 3
        stoppnivaaDetaljer.linjeId = BigInteger.valueOf(21423L)
        stoppnivaaDetaljer.sats = BigDecimal.ZERO
        stoppnivaaDetaljer.typeSats = ""
        stoppnivaaDetaljer.antallSats = BigDecimal.valueOf(0)
        stoppnivaaDetaljer.saksbehId = "5323"
        if (sequence == 3) {
            stoppnivaaDetaljer.uforeGrad = BigInteger.valueOf(100L)
        } else {
            stoppnivaaDetaljer.uforeGrad = BigInteger.ZERO
        }
        stoppnivaaDetaljer.kravhaverId = ""
        stoppnivaaDetaljer.delytelseId = ""
        stoppnivaaDetaljer.bostedsenhet = "4643"
        stoppnivaaDetaljer.skykldnerId = ""
        if (sequence == 2) {
            stoppnivaaDetaljer.klassekode = "KL_KODE_FEIL_KORTTID"
        } else {
            stoppnivaaDetaljer.klassekode = oppdragsperiode.kodeKlassifik
        }
        stoppnivaaDetaljer.klasseKodeBeskrivelse = "DUMMY"
        if (sequence == 2) {
            stoppnivaaDetaljer.typeKlasse = "FEIL"
        } else {
            stoppnivaaDetaljer.typeKlasse = "YTEL"
        }
        stoppnivaaDetaljer.typeKlasseBeskrivelse = "DUMMY"
        if (erRefusjon!! && refunderesOrgNr != null) {
            stoppnivaaDetaljer.refunderesOrgNr = refunderesOrgNr
        } else stoppnivaaDetaljer.refunderesOrgNr = ""
        return stoppnivaaDetaljer
    }

    private fun setBeløp(antallVirkedager: Int, oppdragsperiode: Periode, sequence: Int): BigDecimal {
        val belop: BigDecimal = if (oppdragsperiode.typeSats == "DAG") {
            oppdragsperiode.sats!!.multiply(BigDecimal.valueOf(antallVirkedager.toLong()))
        } else {
            oppdragsperiode.sats!!
        }
        return if (oppdragsperiode.periodeType == PeriodeType.OPPH) {
            if (sequence == 3) {
                belop.negate()
            } else {
                belop
            }
        } else {
            if (sequence == 2) {
                if (oppdragsperiode.typeSats == "DAG") {
                    belop.subtract(oppdragsperiode.oldSats!!.multiply(BigDecimal.valueOf(antallVirkedager.toLong()))).negate()
                } else { belop.subtract(oppdragsperiode.oldSats).negate() }
            } else if (sequence == 3) {
                if (oppdragsperiode.typeSats == "DAG") {
                    oppdragsperiode.oldSats!!.multiply(BigDecimal.valueOf(antallVirkedager.toLong())).negate()
                } else { oppdragsperiode.oldSats!!.negate() }
            } else belop
        }
    }

    companion object {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fun splittOppIPeriodePerMnd(oppdragsperiode: Periode): List<Periode> {
            val perioder: MutableList<Periode> = ArrayList()
            require(!oppdragsperiode.tom.isBefore(oppdragsperiode.fom)) {
                "Startdato " + oppdragsperiode.fom.format(
                    dateTimeFormatter
                ) + " kan ikke være etter sluttdato " + oppdragsperiode.tom.format(dateTimeFormatter)
            }
            var dato = oppdragsperiode.fom
            while (!dato.isAfter(oppdragsperiode.tom)) {
                val sisteDagIMnd = YearMonth.from(dato).atEndOfMonth()
                dato = if (sisteDagIMnd.isBefore(oppdragsperiode.tom)) {
                    perioder.add(Periode(dato, sisteDagIMnd))
                    sisteDagIMnd.plusDays(1)
                } else {
                    perioder.add(Periode(dato, oppdragsperiode.tom))
                    oppdragsperiode.tom.plusDays(1)
                }
            }
            return perioder
        }
    }
}
