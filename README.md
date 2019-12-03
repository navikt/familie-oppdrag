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

## Kontaktinfo
For NAV-interne kan henvendelser om appen rettes til #team-familie på slack. Ellers kan man opprette et issue her på github.
