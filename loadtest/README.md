# loadtest/ — ambiente di stress test **usa e getta** e isolato

Serve a misurare **quanto regge** Asteria e a **provare i fix DoS** (es. il kill
guild-badge) **senza toccare la produzione**.

> ⚠️ **REGOLA UNICA**: si punta SOLO a un'istanza locale/usa-e-getta
> (`localhost`). Gli script **rifiutano** qualsiasi target `*.asteriacore.online`
> o non privato. Non usare mai questo contro la VPS condivisa: dev e prod ci
> girano insieme, dietro Cloudflare.

## 1. Stack isolato (Docker) — la "istanza usa e getta"

```bash
cd loadtest
cp .env.example .env            # secret finti, va bene così: è usa e getta
docker compose up -d --build    # db + api + emu isolati, porte su localhost
docker compose ps
```

Porte esposte SOLO su `127.0.0.1`:
- API CMS  → http://localhost:18092
- EMU game → tcp://localhost:13000  (WebSocket)
- EMU health → http://localhost:19090/readyz

Teardown completo (cancella tutto, è usa e getta):
```bash
docker compose down -v
```

## 2. Load test HTTP (vegeta) — endpoint pesanti dell'API

Installa vegeta (`brew install vegeta` / `go install github.com/tsenart/vegeta/v12@latest`).

```bash
cd loadtest/http
API=http://localhost:18092 ./run-vegeta.sh 200 30s   # 200 req/s per 30s
```
Colpisce gli endpoint che il pentest DoS ha segnalato: `/profile/:username`
(fan-out ~13 query), `/community/rooms?offset=…`, `/api/v2/community/stats`,
`/api/v2/auth/login` (deve rispondere 429 = rate-limit sano). Stampa latenze
p50/p95/p99, throughput e distribuzione dei codici.

## 3. Load test HTTP (k6) — scenario a rampa

Installa k6 (`brew install k6`).
```bash
cd loadtest/http
API=http://localhost:18092 k6 run k6.js
```

## 4. Flood a livello connessione/framing EMU (Node)

```bash
cd loadtest/emu
EMU_WS=ws://localhost:13000 EMU_HEALTH=http://localhost:19090 \
  node flood.mjs --conns 300 --seconds 30
```
Apre N WebSocket concorrenti, invia frame piccoli e frame **oversize** (esercita
il cap di framing e il reaper pre-login), poi controlla `/readyz` prima/dopo e
dice se l'EMU è **sopravvissuto**. È uno strumento di **resilienza**, non contiene
exploit "armati": questo è un repo pubblico e il codice Arcturus è condiviso da
altri server.

> Il **kill guild-badge da 1 packet** (il DoS critico trovato) è chiuso dalla
> **PR #222** (cap `count`). Non lo weaponizziamo qui: si verifica a livello di
> codice (`RequestGuildBuyEvent`: `if (count < 0 || count > 32) return;`) o
> manualmente in un ambiente dev con una sessione autenticata. Pubblicare un
> exploit turnkey di kill-da-1-packet sarebbe irresponsabile.

## Come leggere i risultati
- **p95/p99 latenza** che esplode = punto di saturazione (di solito il pool DB
  a 20 connessioni o la CPU del bcrypt sul login).
- **429** su `/login` = il rate-limit funziona (voluto).
- **500/502/timeout** in massa = hai trovato il collo di bottiglia: annota
  req/s e apri un'issue con i numeri.
