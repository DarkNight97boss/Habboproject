# Multi-stage Dockerfile per Arcturus Morningstar 3.5.5.
#
# Stage 1 (build): Maven con JDK 11 (cap delle dipendenze runtime).
# Stage 2 (runtime): JRE 11 slim, niente Maven, niente sorgenti, niente shell tools
#                    di troppo. Utente non-root, healthcheck integrato.
#
# Build:
#   docker build -t habbo-emu:local -f EMU/Dockerfile EMU
#
# Run (sviluppo, host network = DB su localhost):
#   docker run --rm -it --network=host \
#     -v $(pwd)/EMU/config.ini:/habbo/config.ini:ro \
#     -v $(pwd)/EMU/logging:/habbo/logging \
#     habbo-emu:local
#
# Run (produzione con docker-compose): vedi docker-compose.yml in repo root.

# ===== Stage 1: build =====
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /src

# Cache layer dipendenze: copia solo pom.xml per primo, scarica le dep,
# poi copia i sorgenti. In questo modo `docker build` riusa la cache delle
# dipendenze se non cambia il pom.xml.
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

# Copia il resto e build (skip test = i nostri test li corre la CI; il
# Docker image deve buildare anche se i test sono fragili in container).
COPY src ./src
RUN mvn -B -DskipTests package \
 && mv target/Habbo-*-jar-with-dependencies.jar /app.jar

# ===== Stage 2: runtime =====
FROM eclipse-temurin:11-jre-jammy

# Crea utente non-root + home dir. Il processo Java girera' come "habbo".
RUN groupadd -r habbo \
 && useradd -r -g habbo -d /habbo -m -s /bin/false habbo \
 && mkdir -p /habbo/logging /habbo/dumps \
 && chown -R habbo:habbo /habbo

WORKDIR /habbo

# Copia binario + config skeleton. Il config reale viene montato come volume
# read-only (cosi' non finisce nell'immagine).
COPY --from=build --chown=habbo:habbo /app.jar /habbo/app.jar
COPY --chown=habbo:habbo config.ini.example /habbo/config.ini.example

USER habbo

# JVM tuning per >5k utenti simultanei. Override-abile via env var JAVA_OPTS.
# Razionale:
#   -Xms=-Xmx  : niente resize pause (alloca subito tutto)
#   G1GC       : low-pause GC (compromesso per real-time game)
#   100ms      : target pause - oltre questo non si sente fluido
#   StringDedup: G1 dedupa stringhe duplicate (chat ripetute -> -10% heap)
#   AlwaysPreTouch: touch pages all'avvio invece di on-fault (un po' piu'
#                   lento il boot, ma pause GC piu' prevedibili)
#   ExitOnOOM  : crash subito invece di degradare zombico
#   leakDetection.level=disabled : Netty per default scan ByteBuf in prod
#                                  (overhead 1-2% CPU); off in prod.
ENV JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC -XX:MaxGCPauseMillis=100 \
  -XX:+UseStringDeduplication -XX:+AlwaysPreTouch \
  -XX:MaxMetaspaceSize=256m \
  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/habbo/dumps/ \
  -XX:+ExitOnOutOfMemoryError \
  -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true \
  -Dio.netty.allocator.type=pooled \
  -Dio.netty.leakDetection.level=disabled"

# Porte: game, RCON, health endpoint Prometheus
EXPOSE 3000 3001 9090

# Healthcheck via /readyz (definito da HealthEndpoint.java).
# Restart automatico se Netty crashes o DB cade per >3 tentativi.
HEALTHCHECK --interval=20s --timeout=3s --start-period=90s --retries=3 \
  CMD wget -qO- --spider http://127.0.0.1:9090/readyz || exit 1

# `exec` -> Java diventa PID 1 -> riceve SIGTERM da docker stop -> shutdown
# hook in Emulator.java parte -> Server.stop() drena per 15s (Wave Prod).
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /habbo/app.jar"]
