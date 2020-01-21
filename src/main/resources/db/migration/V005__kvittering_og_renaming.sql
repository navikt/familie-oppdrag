ALTER TABLE oppdrag_protokoll
    RENAME TO oppdrag_lager;

ALTER TABLE oppdrag_lager
    RENAME COLUMN input_data TO utbetalingsoppdrag;
ALTER TABLE oppdrag_lager
    RENAME COLUMN melding TO utgaaende_oppdrag;

ALTER TABLE oppdrag_lager
    ALTER COLUMN utbetalingsoppdrag TYPE json USING to_json(utbetalingsoppdrag),
    ALTER COLUMN utgaaende_oppdrag TYPE json USING to_json(utgaaende_oppdrag);

ALTER TABLE oppdrag_lager
    ADD COLUMN kvitteringsmelding json;