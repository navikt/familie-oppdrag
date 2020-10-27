package no.nav.familie.oppdrag.config

import no.nav.sbl.dialogarena.common.cxf.CXFClient
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfig(@Value("\${SECURITYTOKENSERVICE_URL}") stsUrl: String,
                    @Value("\${CREDENTIAL_USERNAME}") systemuserUsername: String,
                    @Value("\${CREDENTIAL_PASSWORD}") systemuserPwd: String,
                    @Value("\${OPPDRAG_SERVICE_URL}") private val simulerFpServiceUrl: String) {

    init {
        System.setProperty("no.nav.modig.security.sts.url", stsUrl)
        System.setProperty("no.nav.modig.security.systemuser.username", systemuserUsername)
        System.setProperty("no.nav.modig.security.systemuser.password", systemuserPwd)
    }

    @Bean
    fun SimulerFpServicePort(): SimulerFpService =
            CXFClient(SimulerFpService::class.java)
                    .address(simulerFpServiceUrl)
                    .timeout(20000, 20000)
                    .configureStsForSystemUser()
                    .withOutInterceptor(LoggingOutInterceptor())
                    .build()
}