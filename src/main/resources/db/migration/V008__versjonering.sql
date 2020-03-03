ALTER TABLE oppdrag_lager
    DROP CONSTRAINT oppdrag_protokoll_pkey,
    ADD COLUMN versjon BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT oppdrag_lager_pkey PRIMARY KEY (person_ident, behandling_id, fagsystem, versjon);