CREATE TABLE simulering_lager
(
  id                    UUID PRIMARY KEY,
  fagsak_id             text NOT NULL,
  behandling_id         text NOT NULL,
  fagsystem             text NOT NULL,
  opprettet_tidspunkt   timestamp(3) NOT NULL DEFAULT localtimestamp,
  utbetalingsoppdrag    text NOT NULL,
  request_xml           text NOT NULL,
  response_xml          text
);

CREATE INDEX simuleringsid_idx ON simulering_lager (behandling_id, fagsak_id, fagsystem);
