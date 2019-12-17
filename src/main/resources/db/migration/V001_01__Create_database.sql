CREATE TABLE oppdrag_protokoll
(
  serienummer         bigserial PRIMARY KEY,
  id                  varchar(50)  NOT NULL,
  melding             text         NOT NULL,
  status              varchar(150) NOT NULL DEFAULT 'LAGT_PÅ_KØ',
  opprettet_tidspunkt timestamp(3) NOT NULL DEFAULT localtimestamp
);

CREATE INDEX status_index ON oppdrag_protokoll (status);
