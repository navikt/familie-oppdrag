package no.nav.familie.oppdrag.repository

import java.util.Optional
import java.util.UUID
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TidligereKjørteGrensesnittavstemmingerRepository : InsertUpdateRepository<TidligereKjørteGrensesnittavstemminger>,
    CrudRepository<TidligereKjørteGrensesnittavstemminger, UUID> {

    override fun findById(id: UUID): Optional<TidligereKjørteGrensesnittavstemminger>
}
