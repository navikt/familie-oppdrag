package no.nav.familie.oppdrag.service

import no.nav.familie.kontrakter.felles.oppdrag.*
import no.nav.familie.oppdrag.avstemming.AvstemmingSender
import no.nav.familie.oppdrag.konsistensavstemming.KonsistensavstemmingMapper
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.nav.familie.oppdrag.repository.UtbetalingsoppdragForKonsistensavstemming
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class KonsistensavstemmingService(
        private val avstemmingSender: AvstemmingSender,
        private val oppdragLagerRepository: OppdragLagerRepository,
) {

    @Deprecated("Bruk KonsistensavstemmingRequestV2")
    fun utførKonsistensavstemming(request: KonsistensavstemmingRequest) {
        val oppdragIdListe = request.oppdragIdListe
        val fagsystem = request.fagsystem
        val avstemmingstidspunkt = request.avstemmingstidspunkt

        val utbetalingsoppdrag = oppdragIdListe.map { id ->
            val oppdragLager = oppdragLagerRepository.hentAlleVersjonerAvOppdrag(OppdragId(fagsystem,
                                                                                           id.personIdent,
                                                                                           id.behandlingsId.toString()))
                    .find { oppdragLager -> oppdragLager.status == OppdragStatus.KVITTERT_OK
                                            || oppdragLager.status == OppdragStatus.KVITTERT_MED_MANGLER }
            oppdragLagerRepository.hentUtbetalingsoppdrag(OppdragId(fagsystem, id.personIdent, id.behandlingsId.toString()),
                                                          oppdragLager!!.versjon)
        }

        utførKonsistensavstemming(fagsystem, utbetalingsoppdrag, avstemmingstidspunkt)
    }

    private fun utførKonsistensavstemming(
            fagsystem: String,
            utbetalingsoppdrag: List<Utbetalingsoppdrag>,
            avstemmingstidspunkt: LocalDateTime,
    ) {
        val konsistensavstemmingMapper = KonsistensavstemmingMapper(fagsystem, utbetalingsoppdrag, avstemmingstidspunkt)
        val meldinger = konsistensavstemmingMapper.lagAvstemmingsmeldinger()

        if (meldinger.isEmpty()) {
            LOG.info("Ingen oppdrag å utføre konsistensavstemming for")
            return
        }

        LOG.info("Utfører konsistensavstemming for id ${konsistensavstemmingMapper.avstemmingId}, " +
                 "antall meldinger er ${meldinger.size} (inkl. de tre meldingene start, totalinfo og stopp)")
        meldinger.forEach {
                avstemmingSender.sendKonsistensAvstemming(it)
        }

        LOG.info("Fullført konsistensavstemming for id ${konsistensavstemmingMapper.avstemmingId}")
    }

    fun utførKonsistensavstemming(request: KonsistensavstemmingRequestV2) {
        val fagsystem = request.fagsystem
        val avstemmingstidspunkt = request.avstemmingstidspunkt

        val perioderPåBehandling = request.perioderForBehandlinger.map { it.behandlingId to it.perioder }.toMap()
        verifyUnikeBehandlinger(perioderPåBehandling, request)

        val utbetalingsoppdragForKonsistensavstemming =
            oppdragLagerRepository.hentUtbetalingsoppdragForKonsistensavstemming(fagsystem, perioderPåBehandling.keys)

        val utbetalingsoppdrag = leggAktuellePerioderISisteUtbetalingsoppdraget(utbetalingsoppdragForKonsistensavstemming,
                                                                                perioderPåBehandling)

        utførKonsistensavstemming(fagsystem, utbetalingsoppdrag, avstemmingstidspunkt)
    }

    /**
     * Legger inn alle (filtrerte) perioder for en gitt fagsak i det siste utbetalingsoppdraget
     */
    private fun leggAktuellePerioderISisteUtbetalingsoppdraget(
            utbetalingsoppdrag: List<UtbetalingsoppdragForKonsistensavstemming>,
            perioderPåBehandling: Map<String, Set<Long>>,
    ): List<Utbetalingsoppdrag> {
        val utbetalingsoppdragPåFagsak = utbetalingsoppdrag.groupBy { it.fagsakId }

        return utbetalingsoppdragPåFagsak.map { (saksnummer, utbetalingsoppdragListe) ->
            val senesteUtbetalingsoppdrag = utbetalingsoppdragListe.maxByOrNull { oppdrag ->
                oppdrag.utbetalingsoppdrag.utbetalingsperiode.maxOf { it.periodeId }
            }?.utbetalingsoppdrag ?: error("Finner ikke seneste behandling for fagsak=$saksnummer")

            val behandlingsIderForFagsak = utbetalingsoppdragListe.map { it.behandlingId }.toSet()

            val aktuellePeriodeIderForFagsak =
                    perioderPåBehandling.filter { behandlingsIderForFagsak.contains(it.key) }.values.flatten()

            val perioderTilKonsistensavstemming = utbetalingsoppdragListe.flatMap { it.utbetalingsoppdrag.utbetalingsperiode
                    .filter { utbetalingsperiode -> aktuellePeriodeIderForFagsak.contains(utbetalingsperiode.periodeId) }
            }

            senesteUtbetalingsoppdrag.copy(utbetalingsperiode = perioderTilKonsistensavstemming)
        }
    }

    private fun verifyUnikeBehandlinger(periodeIderPåBehandling: Map<String, Set<Long>>, request: KonsistensavstemmingRequestV2) {
        if (periodeIderPåBehandling.size != request.perioderForBehandlinger.size) {
            val duplikateBehandlinger =
                request.perioderForBehandlinger.map { it.behandlingId }.groupingBy { it }.eachCount().filter { it.value > 1 }
            error("Behandling finnes flere ganger i requesten: ${duplikateBehandlinger.keys}")
        }
    }

    companion object {

        val LOG: Logger = LoggerFactory.getLogger(KonsistensavstemmingService::class.java)
    }
}