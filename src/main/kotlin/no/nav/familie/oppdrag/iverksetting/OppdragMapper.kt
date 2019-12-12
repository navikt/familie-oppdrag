package no.nav.familie.oppdrag.iverksetting

import no.trygdeetaten.skjema.oppdrag.ObjectFactory
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import no.trygdeetaten.skjema.oppdrag.Oppdrag110
import org.springframework.stereotype.Component

@Component
class OppdragMapper {

    private val objectFactory = ObjectFactory()

    fun tilOppdrag110(fagsakId: String): Oppdrag110 {

        val oppdragsEnhet = objectFactory.createOppdragsEnhet120().apply {
            enhet = OppdragSkjemaConstants.ENHET
            typeEnhet = OppdragSkjemaConstants.ENHET_TYPE
            datoEnhetFom = OppdragSkjemaConstants.ENHET_DATO_FOM.toXMLDate()
        }

        val oppdragsLinje = objectFactory.createOppdragsLinje150().apply {
            fradragTillegg = OppdragSkjemaConstants.FRADRAG_TILLEGG
            brukKjoreplan = OppdragSkjemaConstants.BRUK_KJØREPLAN
        }

        val oppdrag110 = objectFactory.createOppdrag110().apply {
            kodeAksjon = OppdragSkjemaConstants.KODE_AKSJON
            datoOppdragGjelderFom = OppdragSkjemaConstants.OPPDRAG_GJELDER_DATO_FOM.toXMLDate()
            utbetFrekvens = UtbetalingsfrekvensKode.MÅNEDLIG.kode
            fagsystemId = fagsakId
            oppdragsEnhet120.add(oppdragsEnhet)
            oppdragsLinje150.add(oppdragsLinje)
        }

        return oppdrag110
    }

    fun tilOppdrag(oppdrag110: Oppdrag110): Oppdrag {
        return objectFactory.createOppdrag().apply {
            this.oppdrag110 = oppdrag110
        }
    }
}