# EMU per il load test (usa e getta). JDK 25 come la prod (EMU/Dockerfile in repo
# è ancora JDK 11 e non compilerebbe <release>25</release>; qui non lo tocchiamo
# per non far scattare un rebuild/restart dell'EMU di produzione).
# Context di build: root del repo. Uso: referenziato da loadtest/docker-compose.yml
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /src
COPY EMU/pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline || true
COPY EMU/ ./
RUN mvn -B -DskipTests package \
 && mv target/Habbo-*-jar-with-dependencies.jar /app.jar

FROM eclipse-temurin:25-jre
RUN useradd -r -m -d /habbo habbo
WORKDIR /habbo
COPY --from=build --chown=habbo:habbo /app.jar /habbo/app.jar
USER habbo
EXPOSE 3000 3001 9090
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+ExitOnOutOfMemoryError -Dio.netty.leakDetection.level=disabled"
CMD ["sh","-c","java $JAVA_OPTS -jar /habbo/app.jar"]
