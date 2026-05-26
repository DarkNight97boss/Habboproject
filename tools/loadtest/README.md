# Load test tool

Strumento minimale per misurare quanto il game socket (Netty, porta 3000)
regge sotto connessioni concorrenti.

NON simula un client Nitro completo (servirebbe l'implementazione del wire
protocol Habbo); valida invece i livelli "sotto":

  - boss event-loop accept rate
  - worker event-loop ratelimit pre-login
  - HikariCP / OS file-descriptor cap
  - kernel `net.ipv4.tcp_max_syn_backlog`

Cose che misuriamo:

  - tempo medio di connect TCP
  - p50/p95/p99 di connect time
  - % di connect rifiutati (ECONNREFUSED) o timed-out
  - throughput max sostenuto

## Compile

E' un modulo Maven separato per non sporcare il classpath dell'EMU:

    cd tools/loadtest
    mvn -B -DskipTests package

Produce `target/loadtest.jar`.

## Run

Esempio: 2000 connessioni concorrenti per 30s contro `localhost:3000`:

    java -jar target/loadtest.jar \
        --host 127.0.0.1 --port 3000 \
        --connections 2000 --duration 30s

Output (stdout):

    [+0s]  connected=  47  failed=  0  inflight=  0
    [+5s]  connected=2000  failed= 12  inflight=1988
    ...
    === Summary ===
    Total attempts:    2012
    Connected:         2000 (99.4%)
    Failed:              12 (0.6%)
    Connect p50:         3 ms
    Connect p95:        18 ms
    Connect p99:        47 ms
    Connect max:        92 ms

## Cosa cercare nei risultati

  - **failed > 0.5%** -> kernel/somaxconn troppo basso o per-IP limit del
    server, oppure CPU EMU saturated.
  - **p99 > 200ms** -> boss event-loop saturo, considera +1 boss thread.
  - **OS file descriptors esauriti** -> `ulimit -n 100000` (vedi Fase 5 nel
    deliverable).

## Limiti

  - Apre socket TCP grezzi, non manda packet handshake -> per il server
    sono "client che si connettono ma non parlano". Dopo qualche secondo
    `networking.auth.timeout.seconds=30` li kicka. Va bene per misurare
    accept rate, non goodput applicativo.
  - Tutte le connessioni partono dallo stesso IP locale; se sul server
    hai `networking.max.connections.per.ip > 0`, alza il limite o usa
    container multipli come sorgenti.
