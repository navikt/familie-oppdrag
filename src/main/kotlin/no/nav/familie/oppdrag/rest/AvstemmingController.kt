package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.oppdrag.grensesnittavstemming.AvstemmingMapper
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.Valid

class AvstemmingController (@Autowired val oppdragProtokollRepository: OppdragProtokollRepository) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/grensesnittavstemming"])
    fun utførGrensesnittavstemming(@Valid @RequestBody fomTidspunkt: LocalDateTime, tomTidspunkt: LocalDateTime, fagOmråde: String): ResponseEntity<Ressurs<String>> {
        val oppdragSomSkalAvstemmes = oppdragProtokollRepository.hentIverksettingerForGrensesnittavstemming(fomTidspunkt, tomTidspunkt, fagOmråde)

        val meldinger = AvstemmingMapper(oppdragSomSkalAvstemmes, fagOmråde).lagAvstemmingsmeldinger()

        // send meldinger på MQ

        return ResponseEntity.ok().body(Ressurs.Companion.success("Grensesnittavstemming sendt ok"))
    }

}