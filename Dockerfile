FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY credit-simulator-core/pom.xml credit-simulator-core/
COPY credit-simulator-cli/pom.xml credit-simulator-cli/
RUN mvn -B -ntp -DskipTests dependency:go-offline
COPY credit-simulator-core/src credit-simulator-core/src
COPY credit-simulator-cli/src credit-simulator-cli/src
RUN mvn -B -ntp package

FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.title="credit-simulator" \
      org.opencontainers.image.description="Console application to simulate vehicle loan installments" \
      org.opencontainers.image.source="https://github.com/Fadhil-AS/credit_simulator"

RUN addgroup -S simulator && adduser -S -G simulator -u 10001 simulator
WORKDIR /app

COPY --from=build /workspace/credit-simulator-cli/target/credit-simulator.jar /app/credit-simulator.jar
COPY --chmod=0755 docker/entrypoint.sh /usr/local/bin/credit_simulator

ENV CREDIT_SIMULATOR_SHEET_DIR=/data/sheets \
    CREDIT_SIMULATOR_MOCK_DIR=/data/mock
RUN mkdir -p /data/sheets /data/mock && chown -R simulator:simulator /data

USER simulator
VOLUME ["/data"]
ENTRYPOINT ["credit_simulator"]
