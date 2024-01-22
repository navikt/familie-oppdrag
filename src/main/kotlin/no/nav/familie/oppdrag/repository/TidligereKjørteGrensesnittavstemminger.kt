package no.nav.familie.oppdrag.repository

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("tidligere_kjoerte_grensesnittavstemminger")
class TidligereKjørteGrensesnittavstemminger(
    @Id val id: UUID
)