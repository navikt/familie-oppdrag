package no.nav.familie.oppdrag.rest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.oppdrag.grensesnittavstemming.AvstemmingMapper
import no.nav.familie.oppdrag.repository.OppdragProtokollRepository
import no.nav.familie.oppdrag.service.AvstemmingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class AvstemmingController (@Autowired val oppdragProtokollRepository: OppdragProtokollRepository,
                            @Autowired val avstemmingService: AvstemmingService) {

    @PostMapping(path = ["/grensesnittavstemming/{fagsystem}"])
    fun sendGrensesnittavstemming(@PathVariable("fagsystem") fagsystem: String,
                                  @RequestParam("fom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fom: LocalDateTime,
                                  @RequestParam("tom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) tom: LocalDateTime
    ): ResponseEntity<Ressurs<String>> {
        OppdragController.LOG.info("Grensesnittavstemming: Kjører for $fagsystem-oppdrag for $fom til $tom")
        val oppdragSomSkalAvstemmes = oppdragProtokollRepository.hentIverksettingerForGrensesnittavstemming(fom, tom, fagsystem)

        val meldinger = AvstemmingMapper(oppdragSomSkalAvstemmes, fagsystem).lagAvstemmingsmeldinger()

        avstemmingService.utførGrensesnittavstemming(meldinger)

        return ResponseEntity.ok().body(Ressurs.success("Grensesnittavstemming sendt ok"))
    }

}