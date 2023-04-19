package no.nav.familie.oppdrag.tss

data class TssSamhandlerIdent(
    val ident: String,
    val type: TssSamhandlerIdentType,
)
enum class TssSamhandlerIdentType {
    ORGNR, TSS
}
