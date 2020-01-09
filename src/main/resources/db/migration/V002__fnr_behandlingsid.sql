TRUNCATE oppdrag_protokoll;

ALTER TABLE oppdrag_protokoll
    DROP COLUMN id;

ALTER TABLE oppdrag_protokoll
    ADD COLUMN person_id VARCHAR(50) NOT NULL,
    ADD COLUMN fagsak_id VARCHAR(50) NOT NULL,
    ADD COLUMN behandling_id VARCHAR(50) NOT NULL,
    ADD COLUMN fagsystem VARCHAR(10) NOT NULL,
    ADD COLUMN input_data text NOT NULL;
