FROM navikt/java:17-appdynamics

ENV APPD_ENABLED=TRUE
COPY init.sh /init-scripts/init.sh
COPY ./target/familie-oppdrag.jar "app.jar"
