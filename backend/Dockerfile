
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /src

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jre-headless \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home appuser

WORKDIR /app

COPY --from=build /src/target/college-api-jar-with-dependencies.jar /app/college-api.jar

USER appuser

EXPOSE 8080

CMD ["java", "-jar", "/app/college-api.jar"]
