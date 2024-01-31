package no.nav.familie.oppdrag

import no.nav.familie.oppdrag.config.ApplicationConfig
import org.springframework.boot.builder.SpringApplicationBuilder

object DevLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val app =
            SpringApplicationBuilder(ApplicationConfig::class.java)
                .profiles("dev")
        app.run(*args)
    }
}
