package no.nav.familie.oppdrag.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("tidligere_kjoerte_grensesnittavstemminger")
class TidligereKjørtGrensesnittavstemming(
    @Id val id: UUID,
)
