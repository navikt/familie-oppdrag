package no.nav.familie.oppdrag

import no.nav.familie.sikkerhet.context.FamilieFellesNavTokenSupportKonfigurasjon
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import
import org.springframework.jms.annotation.EnableJms

@SpringBootApplication
@EnableJms
@Import(FamilieFellesNavTokenSupportKonfigurasjon::class)
class Launcher

fun main(args: Array<String>) {
    SpringApplication.run(Launcher::class.java, *args)
}
