ALTER TABLE oppdrag_lager
    ALTER COLUMN opprettet_tidspunkt TYPE timestamp(6),
    ALTER COLUMN avstemming_tidspunkt TYPE timestamp(6),
    ALTER COLUMN utgaaende_oppdrag TYPE text;