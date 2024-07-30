package no.nav.familie.oppdrag.config

import no.nav.common.cxf.CXFClient
import no.nav.common.cxf.StsConfig
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerFpService
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfig(
    @Value("\${SECURITYTOKENSERVICE_URL}") private val stsUrl: String,
    @Value("\${SERVICEUSER_USERNAME}") private val systemuserUsername: String,
    @Value("\${SERVICEUSER_PASSWORD}") private val systemuserPwd: String,
    @Value("\${OPPDRAG_SERVICE_URL}") private val simulerFpServiceUrl: String,
) {
    @Bean
    fun stsConfig(): StsConfig =
        StsConfig
            .builder()
            .url(stsUrl)
            .username(systemuserUsername)
            .password(systemuserPwd)
            .build()

    @Bean
    fun simulerFpServicePort(): SimulerFpService =
        CXFClient(SimulerFpService::class.java)
            .address(simulerFpServiceUrl)
            .timeout(20000, 20000)
            .configureStsForSystemUser(stsConfig())
            .withOutInterceptor(LoggingOutInterceptor())
            .build()
}
