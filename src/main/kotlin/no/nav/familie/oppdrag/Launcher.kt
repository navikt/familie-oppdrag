package no.nav.familie.oppdrag

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.jms.annotation.EnableJms

@SpringBootApplication(scanBasePackages = ["no.nav.familie.oppdrag"])
@EnableJms
class Launcher

fun main(args: Array<String>) {
    SpringApplication.run(Launcher::class.java, *args)
}