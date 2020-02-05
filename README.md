# familie-oppdrag
Generell proxy mot Oppdragsystemet (OS) for familie-ytelsene

## Bygging
Bygging gjøres med `mvn clean install`. 

## Kjøring lokalt
For å kjøre opp appen lokalt kan en kjøre `DevLauncher` med Spring-profilen `dev` satt.
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

## Teste i preprod, f.eks Postman

### Autentisering

Nederst på [denne siden](https://confluence.adeo.no/display/TFA/Teknisk+veivalg) finnes oppdatert url for å genere nytt token. I skrivende stund er det :

```
curl -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=client_credentials&client_id=<client-id>&client_secret=<client-secret>&scope=api://<destinasjon-client-id>/.default" https://login.microsoftonline.com/navq.onmicrosoft.com/oauth2/v2.0/token`
```
Der manglende verdier finnes i [Vault](https://vault.adeo.no/ui/vault/secrets/kv%2Fpreprod%2Ffss/show/familie-ba-sak/default), under nøkkel `azure.env`:

* `<client-id>`= `<BA_SAK_CLIENT_ID>`
* `<client-secret>` = `<CLIENT_SECRET>`
* `<destinasjon-client-id>` = `<FAMILIE_OPPDRAG_SCOPE>`

Svaret fra curl-kallet vil se noe sånt ut:

```
{"token_type":"Bearer","expires_in":3599,"ext_expires_in":3599,"access_token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IkhsQzBSMTJza3hOWjFXUXdtak9GXzZ0X3RERSJ9.eyJhdWQiOiJiZTI4ZjYzOS02NjE1LTQ2NjMtYjc3NS0yOGNmY2Y1MzhkZGUiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vOTY2YWM1NzItZjViNy00YmJlLWFhODgtYzc2NDE5YzBmODUxL3YyLjAiLCJpYXQiOjE1ODA4MTQzNDksIm5iZiI6MTU4MDgxNDM0OSwiZXhwIjoxNTgwODE4MjQ5LCJhaW8iOiI0Mk5nWVBoMjZLN2plL050TmFjK3JpMStlclR5QWdBPSIsImF6cCI6IjE0NmNjYzY5LTdjZDAtNGI4ZS04NmE1LTE0NDUzNGU1M2EwMCIsImF6cGFjciI6IjEiLCJvaWQiOiIxZTFiYTdjOS1jNDI5LTQzMDktOGZjMy1lMjExNTFkMDBhMTUiLCJzdWIiOiIxZTFiYTdjOS1jNDI5LTQzMDktOGZjMy1lMjExNTFkMDBhMTUiLCJ0aWQiOiI5NjZhYzU3Mi1mNWI3LTRiYmUtYWE4OC1jNzY0MTljMGY4NTEiLCJ1dGkiOiJRTEUtQjFXa1hrLVZtd0tpNmNYQkFBIiwidmVyIjoiMi4wIn0.bQBm4n_xHTGwj31TlCi1quQOdilWsd5PzUuinn40yDsKF8-wDuXyyu1Pwwj1-q4c_szKYDXJRMQDRmBimnCQTB-mw3-gPmy5oYxnQaMBwjpge-zDDVXfN_kzknZtwYX5seO7zpllkN93FfkCkuyCaTKu5ozFc91tl-b3N9NxUyrptRuwexB_w0HHN8jQxC40SpW1hlt5T4UoGrFCAKgoKnuPDGgBfwu54p4ZYW7ZJXhbwSH7y4aL0GGSFlcKTsRfYF2xk4gqhtibIvqk5-lMZIfdej7eujJOXea-hepc_JvhmGxpAT7eTrUiYUKX8c2eJXPz9_9Nvn8xGRzAwICVEw"}
```

Du trenger verdien for `access_token`

### Test med Postman
For å teste i postman kan du prøve med følgende verdier:

* Http-metode: `POST`<br/>
* Url: `https://familie-oppdrag.nais.preprod.local/api/status`<br/>
* Headers:
  * Authorization: `Bearer <access_token>`
  * Content-Type: `application/json`
* Body: `{ "fagsystem": "String","personIdent": "String","behandlingsId": "String"}`

Du bør få et 404-svar som ser slik ut:
```
{
    "data": null,
    "status": "FEILET",
    "melding": "Fant ikke oppdrag med id OppdragId(fagsystem=String, behandlingsId=String)",
    "stacktrace": null
}
```

## Kikke på database i preprod

### Få brukernavn/passord til postgres

1. Åpne [Vault](https://vault.adeo.no/ui/vault/secrets)
2. Åpne konsoll-vinduet i Vault (ikon øverst til høyre)
3. Kjør `vault read postgresql/preprod-fss/creds/familie-oppdrag-readonly`

Resultatet er brukernavn og passord med 8 timers levetid

### Koble til database

Du trenger connection-string med IP-adressen til databasen i preprod hvis du skal kalle fra laptop. I skrivende stund: `jdbc:postgresql://10.183.160.86:5432/familie-oppdrag`



## Kontaktinfo
For NAV-interne kan henvendelser om appen rettes til #team-familie på slack. Ellers kan man opprette et issue her på github.

