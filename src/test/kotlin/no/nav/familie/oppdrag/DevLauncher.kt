package no.nav.familie.oppdrag

import no.nav.familie.oppdrag.config.ApplicationConfig
import org.springframework.boot.builder.SpringApplicationBuilder
import java.util.Properties

object DevLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val properties = Properties()
        properties.put("spring.profiles.active", "dev")
        val app =
            SpringApplicationBuilder(ApplicationConfig::class.java)
                .properties(properties)
        app.run(*args)
    }
}
