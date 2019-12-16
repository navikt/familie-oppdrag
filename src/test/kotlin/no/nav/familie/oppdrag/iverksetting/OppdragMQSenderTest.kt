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
import org.junit.jupiter.api.Test
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate
import java.lang.UnsupportedOperationException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

private const val FAGOMRÅDE_BARNETRYGD = "IT05"
private const val KLASSEKODE_BARNETRYGD = "BAOROSMS"
private const val SATS_BARNETRYGD = 1054
private const val TESTKØ = "DEV.QUEUE.1"
private const val TEST_FAGSAKID = "123456789"

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

    private val jmsTemplate = spyk(JmsTemplate(cf).apply { defaultDestinationName = TESTKØ })

    @Test
    fun skal_sende_oppdrag_når_skrudd_på() {
        val oppdragSender = OppdragSender(jmsTemplate, "true", TESTKØ)
        val fagsakId = oppdragSender.sendOppdrag(lagTestOppdrag())

        assertEquals(TEST_FAGSAKID, fagsakId)
    }

    @Test
    fun skal_ikke_sende_oppdrag_når_skrudd_av() {
        val oppdragSender = OppdragSender(jmsTemplate, "false", TESTKØ)

        Assertions.assertThrows(UnsupportedOperationException::class.java) {
            oppdragSender.sendOppdrag(lagTestOppdrag())
        }

        verify { jmsTemplate wasNot called }
    }

    private fun lagTestOppdrag(): Oppdrag {
        val avstemmingsTidspunkt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS"))
        val objectFactory = ObjectFactory()

        val testOppdragsLinje150 = objectFactory.createOppdragsLinje150().apply {
            kodeEndringLinje = EndringsKode.NY.kode
            vedtakId = avstemmingsTidspunkt
            delytelseId = TEST_FAGSAKID
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
            kodeFagomraade = FAGOMRÅDE_BARNETRYGD
            fagsystemId = TEST_FAGSAKID
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
                nokkelAvstemming = avstemmingsTidspunkt
                kodeKomponent = FAGOMRÅDE_BARNETRYGD
                tidspktMelding = avstemmingsTidspunkt
            }
            oppdragsLinje150.add(testOppdragsLinje150)
        }

        return objectFactory.createOppdrag().apply {
            oppdrag110 = testOppdrag110
        }
    }
}