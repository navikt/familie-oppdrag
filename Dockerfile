FROM ghcr.io/navikt/baseimages/temurin:21-appdynamics

ENV APPD_ENABLED=true
ENV APP_NAME=familie-oppdrag

COPY init.sh /init-scripts/init.sh
COPY ./target/familie-oppdrag.jar "app.jar"
