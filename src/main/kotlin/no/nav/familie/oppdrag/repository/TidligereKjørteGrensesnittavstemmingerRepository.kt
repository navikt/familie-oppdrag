package no.nav.familie.oppdrag.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TidligereKjørteGrensesnittavstemmingerRepository :
    InsertUpdateRepository<TidligereKjørtGrensesnittavstemming>,
    CrudRepository<TidligereKjørtGrensesnittavstemming, UUID>
