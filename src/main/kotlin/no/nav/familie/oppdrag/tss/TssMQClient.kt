package no.nav.familie.oppdrag.tss

import no.nav.familie.oppdrag.iverksetting.Jaxb
import no.rtv.namespacetss.ObjectFactory
import no.rtv.namespacetss.SamhandlerIDataB910Type
import no.rtv.namespacetss.SamhandlerIDataB985Type
import no.rtv.namespacetss.TssSamhandlerData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jms.JmsException
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session

@Service
class TssMQClient(@Qualifier("jmsTemplateTss") private val jmsTemplateTss: JmsTemplate) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private fun kallTss(rawRequest: String): String {
        val uuid = UUID.randomUUID().toString()
        "Kaller TSS med jmsCorrelationId=$uuid".apply {
            logger.info(this)
            secureLogger.info("$this request=$rawRequest")
        }
        try {
            val response: Message? = jmsTemplateTss.sendAndReceive { session: Session ->
                val requestMessage = session.createTextMessage(rawRequest)
                requestMessage.jmsCorrelationID = uuid
                requestMessage
            }

            return if (response == null) {
                logger.error("En feil oppsto i kallet til TSS. Response var null (timeout?)")
                throw TssConnectionException("En feil oppsto i kallet til TSS. Response var null (timeout?)")
            } else {
                val responseAsString = response.getBody(String::class.java)
                secureLogger.info("Response fra tss=$responseAsString")
                responseAsString
            }
        } catch (exception: Exception) {
            logger.info("Feil ved sending", exception)
            when (exception) {
                is JmsException, is JMSException -> {
                    throw RuntimeException("En feil oppsto i kallet til TSS", exception)
                }
                else -> throw exception
            }
        }
    }

    fun getOrgInfo(orgNr: String): TssSamhandlerData {
        val objectFactory = ObjectFactory()

        val offIdData = objectFactory.createTidOFF1().apply {
            idOff = orgNr
            kodeIdType = "ORG"
        }
        val samhandlerIDataB910Data = objectFactory.createSamhandlerIDataB910Type().apply {
            brukerID = "familie-oppdrag"
            historikk = "N"
            ofFid = offIdData
        }
        val servicerutiner = objectFactory.createTServicerutiner().apply {
            samhandlerIDataB910 = samhandlerIDataB910Data
        }
        val tssSamhandlerDataTssInputData = objectFactory.createTssSamhandlerDataTssInputData().apply {
            tssServiceRutine = servicerutiner
        }
        val tssSamhandlerData = objectFactory.createTssSamhandlerData().apply {
            tssInputData = tssSamhandlerDataTssInputData
        }
        val xml = Jaxb.tilXml(tssSamhandlerData)
        val rawResponse = kallTss(xml)
        return Jaxb.tilTssSamhandlerData(rawResponse)
    }

    fun getKomplettSamhandlerInfo(orgNr: String): TssSamhandlerData {
        val objectFactory = ObjectFactory()

        val offIdData = objectFactory.createTidOFF1().apply {
            idOff = orgNr
            kodeIdType = "ORG"
        }
        val samhandlerIDataB910Data = objectFactory.createSamhandlerIDataB910Type().apply {
            brukerID = "familie-oppdrag"
            historikk = "N"
            ofFid = offIdData
        }
        val servicerutiner = objectFactory.createTServicerutiner().apply {
            samhandlerIDataB960 = samhandlerIDataB910Data
        }
        val tssSamhandlerDataTssInputData = objectFactory.createTssSamhandlerDataTssInputData().apply {
            tssServiceRutine = servicerutiner
        }
        val tssSamhandlerData = objectFactory.createTssSamhandlerData().apply {
            tssInputData = tssSamhandlerDataTssInputData
        }
        val xml = Jaxb.tilXml(tssSamhandlerData)
        val rawResponse = kallTss(xml)
        return Jaxb.tilTssSamhandlerData(rawResponse)
    }

    fun søkOrgInfo(navn: String): TssSamhandlerData {
        val objectFactory = ObjectFactory()
        val samhandlerIDataB940Data = objectFactory.createSamhandlerIDataB940Type().apply {
            brukerID = "familie-oppdrag"
            navnSamh = navn
            kodeSamhType = "INST"
        }

        val servicerutiner = objectFactory.createTServicerutiner().apply {
            samhandlerIDataB940 = samhandlerIDataB940Data
        }

        val tssSamhandlerDataTssInputData = objectFactory.createTssSamhandlerDataTssInputData().apply {
            tssServiceRutine = servicerutiner
        }
        val tssSamhandlerData = objectFactory.createTssSamhandlerData().apply {
            tssInputData = tssSamhandlerDataTssInputData
        }

        val rawResponse = kallTss(Jaxb.tilXml(tssSamhandlerData))
        return Jaxb.tilTssSamhandlerData(rawResponse)
    }

    fun b960(samhandlerIDataB910Type: SamhandlerIDataB910Type): TssSamhandlerData {
        val objectFactory = ObjectFactory()

        val servicerutiner = objectFactory.createTServicerutiner().apply {
            samhandlerIDataB960 = samhandlerIDataB910Type
        }

        val tssSamhandlerDataTssInputData = objectFactory.createTssSamhandlerDataTssInputData().apply {
            tssServiceRutine = servicerutiner
        }
        val tssSamhandlerData = objectFactory.createTssSamhandlerData().apply {
            tssInputData = tssSamhandlerDataTssInputData
        }

        val rawResponse = kallTss(Jaxb.tilXml(tssSamhandlerData))
        return Jaxb.tilTssSamhandlerData(rawResponse)
    }

    fun søkOrgInfoB985(samhandlerIDataB985Data: SamhandlerIDataB985Type): TssSamhandlerData {
        val objectFactory = ObjectFactory()

        val servicerutiner = objectFactory.createTServicerutiner().apply {
            samhandlerIDataB985 = samhandlerIDataB985Data
        }

        val tssSamhandlerDataTssInputData = objectFactory.createTssSamhandlerDataTssInputData().apply {
            tssServiceRutine = servicerutiner
        }
        val tssSamhandlerData = objectFactory.createTssSamhandlerData().apply {
            tssInputData = tssSamhandlerDataTssInputData
        }

        val rawResponse = kallTss(Jaxb.tilXml(tssSamhandlerData))
        return Jaxb.tilTssSamhandlerData(rawResponse)
    }

    companion object {
        val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")
    }
}
