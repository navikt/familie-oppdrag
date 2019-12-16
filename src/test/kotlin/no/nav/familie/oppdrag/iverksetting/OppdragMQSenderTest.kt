package no.nav.familie.oppdrag.iverksetting

import com.ibm.mq.jms.MQConnectionFactory
import com.ibm.msg.client.wmq.WMQConstants
import io.mockk.called
import io.mockk.spyk
import io.mockk.verify
import no.trygdeetaten.skjema.oppdrag.ObjectFactory
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import no.trygdeetaten.skjema.oppdrag.Oppdrag110
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate
import java.lang.UnsupportedOperationException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

private const val FAGOMRADE_BARNETRYGD = "IT05"
private const val KLASSEKODE_BARNETRYGD = "BAOROSMS"
private const val SATS_BARNETRYGD = 1054

@DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
class OppdragMQSenderTest {

    private val mqConn = MQConnectionFactory().apply {
        hostName = "localhost"
        port = 1414
        channel = "DEV.ADMIN.SVRCONN"
        queueManager = "QM1"
        transportType = WMQConstants.WMQ_CM_CLIENT
    }

    private val cf = UserCredentialsConnectionFactoryAdapter().apply {
        setUsername("admin")
        setPassword("passw0rd")
        setTargetConnectionFactory(mqConn)
    }

    private val jmsTemplate = spyk(JmsTemplate(cf).apply { defaultDestinationName = "DEV.QUEUE.1" })

    @Test
    fun skal_sende_oppdrag_når_skrudd_på() {
        val oppdragSender = OppdragSender(jmsTemplate, "true", "DEV.QUEUE.1")
        val fagsakId = oppdragSender.sendOppdrag(lagOppdrag())

        assertEquals("123456789", fagsakId)
    }

    @Test
    fun skal_ikke_sende_oppdrag_når_skrudd_av() {
        val oppdragSender = OppdragSender(jmsTemplate, "false", "DEV.QUEUE.1")

        Assertions.assertThrows(UnsupportedOperationException::class.java) {
            oppdragSender.sendOppdrag(lagOppdrag())
        }

        verify { jmsTemplate wasNot called }
    }

    private fun lagOppdrag(): Oppdrag {
        val tidspunktFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")
        val avstemmingsTidspunkt = LocalDateTime.now()
        val objectFactory = ObjectFactory()

        val testOppdragsLinje150 = objectFactory.createOppdragsLinje150().apply {
            kodeEndringLinje = EndringsKode.NY.kode
            vedtakId = "Test"
            delytelseId = "123456789"
            kodeKlassifik = KLASSEKODE_BARNETRYGD
            datoVedtakFom = LocalDate.now().toXMLDate()
            datoVedtakTom = LocalDate.now().plusDays(1).toXMLDate()
            sats = SATS_BARNETRYGD.toBigDecimal()
            fradragTillegg = OppdragSkjemaConstants.FRADRAG_TILLEGG
            typeSats = SatsTypeKode.MÅNEDLIG.kode
            brukKjoreplan = OppdragSkjemaConstants.BRUK_KJØREPLAN
            saksbehId = "Z999999"
            utbetalesTilId = "12345678911"
            henvisning = "987654321"
            attestant180.add(objectFactory.createAttestant180().apply {
                attestantId = "Z999999"
            })
        }

        val testOppdrag110 = Oppdrag110().apply {
            kodeAksjon = "1"
            kodeEndring = EndringsKode.NY.kode
            kodeFagomraade = FAGOMRADE_BARNETRYGD
            fagsystemId = "123456789"
            utbetFrekvens = UtbetalingsfrekvensKode.MÅNEDLIG.kode
            oppdragGjelderId = "12345678911"
            datoOppdragGjelderFom = OppdragSkjemaConstants.OPPDRAG_GJELDER_DATO_FOM.toXMLDate()
            saksbehId = "Z999999"
            oppdragsEnhet120.add(objectFactory.createOppdragsEnhet120().apply {
                enhet = OppdragSkjemaConstants.ENHET
                typeEnhet = OppdragSkjemaConstants.ENHET_TYPE
                datoEnhetFom = OppdragSkjemaConstants.ENHET_DATO_FOM.toXMLDate()
            })
            avstemming115 = objectFactory.createAvstemming115().apply {
                nokkelAvstemming = avstemmingsTidspunkt.format(tidspunktFormatter)
                kodeKomponent = FAGOMRADE_BARNETRYGD
                tidspktMelding = avstemmingsTidspunkt.format(tidspunktFormatter)
            }
            oppdragsLinje150.add(testOppdragsLinje150)
        }

        return objectFactory.createOppdrag().apply {
            oppdrag110 = testOppdrag110
        }
    }
}