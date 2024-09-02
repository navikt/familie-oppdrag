FROM ghcr.io/navikt/baseimages/temurin:21

ENV APP_NAME=familie-oppdrag

COPY init.sh /init-scripts/init.sh
COPY ./target/familie-oppdrag.jar "app.jar"
