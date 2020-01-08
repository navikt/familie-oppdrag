# familie-oppdrag
Generell proxy mot Oppdragsystemet (OS) for familie-ytelsene

## Bygging og kjøring lokalt
Bygging gjøres med `mvn clean install`. For å kjøre opp appen lokalt kan en kjøre `DevLauncher` med Spring-profilen `dev` satt.
Appen tilgjengeliggjøres da på `localhost:8087`.
I tillegg må man kjøre opp en MQ-container med docker:
```
docker run \
  --env LICENSE=accept \
  --env MQ_QMGR_NAME=QM1 \
  --publish 1414:1414 \
  --publish 9443:9443 \
  --detach \
  ibmcom/mq
```

Og sette opp en database lokalt:
```
docker run --name familie-oppdrag -e POSTGRES_PASSWORD=test -d -p 5432:5432 postgres
docker ps (finn container id)
docker exec -it <container_id> bash
psql -U postgres
CREATE DATABASE "familie-oppdrag";
```

For å kjøre med denne lokalt må følgende miljøvariabler settes i `application-dev.yml`:
```
spring.datasource.url: jdbc:postgresql://0.0.0.0:5432/familie-oppdrag
spring.datasource.username: postgres
spring.datasource.password: test
```

Les mer om postgres på nav [her](https://github.com/navikt/utvikling/blob/master/PostgreSQL.md). For å hente credentials manuelt, se [her](https://github.com/navikt/utvikling/blob/master/Vault.md). 

## Kontaktinfo
For NAV-interne kan henvendelser om appen rettes til #team-familie på slack. Ellers kan man opprette et issue her på github.
