package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.oppdrag.grensesnittavstemming.AvstemmingMapper
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.familie.oppdrag.service.AvstemmingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.validation.Valid

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class AvstemmingController (@Autowired val oppdragProtokollRepository: OppdragProtokollRepository,
                            @Autowired val avstemmingService: AvstemmingService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/grensesnittavstemming"])
    fun utførGrensesnittavstemming(@Valid @RequestBody fomTidspunkt: LocalDateTime, tomTidspunkt: LocalDateTime, fagOmråde: String): ResponseEntity<Ressurs<String>> {
        val oppdragSomSkalAvstemmes = oppdragProtokollRepository.hentIverksettingerForGrensesnittavstemming(fomTidspunkt, tomTidspunkt, fagOmråde)

        val meldinger = AvstemmingMapper(oppdragSomSkalAvstemmes, fagOmråde).lagAvstemmingsmeldinger()

        avstemmingService.utførGrensesnittavstemming(meldinger)
        return ResponseEntity.ok().body(Ressurs.Companion.success("Grensesnittavstemming sendt ok"))
    }

}