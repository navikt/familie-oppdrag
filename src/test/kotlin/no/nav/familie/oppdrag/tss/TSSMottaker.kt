package no.nav.familie.oppdrag.tss

import no.nav.familie.oppdrag.iverksetting.Jaxb
import no.nav.familie.oppdrag.repository.OppdragLagerRepository
import no.trygdeetaten.skjema.oppdrag.Oppdrag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.jms.TextMessage

@Service
@Profile("!e2e")
class TSSMottaker(
    val oppdragLagerRepository: OppdragLagerRepository,
    @Qualifier("jmsTemplateTss") private val jmsTemplateTss: JmsTemplate
) {

    internal var LOG = LoggerFactory.getLogger(TSSMottaker::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Transactional
    @JmsListener(destination = "\${oppdrag.mq.tss}", containerFactory = "jmsListenerContainerFactory")
    fun mottaKvitteringFraOppdrag(melding: TextMessage) {
        println("${melding.text} ${melding.jmsReplyTo}")

        jmsTemplateTss.send(
            melding.jmsReplyTo
        ) { session -> session.createTextMessage(melding.text) }
    }

    fun lesKvittering(svarFraOppdrag: String): Oppdrag {
        return Jaxb.tilOppdrag(svarFraOppdrag)
    }
}
