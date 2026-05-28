# Game sharding — design doc

Stato: **roadmap**, non implementato. Documento per quando l'emulatore
deve passare da single-process (~3-5k utenti) a >10k+ utenti distribuiti
su piu' processi.

---

## Quando applicarlo

NON serve sotto i 3-4k utenti sostained. Single-EMU su una VPS 16-core +
G1GC tunato (Fase A) regge.

Considera sharding quando:
  - SLI 3 (packet latency p99) sfora >100ms a steady state, NON da picco;
  - Heap >6GB stabile e crescente;
  - CPU >70% sustained su una sola istanza;
  - DB primary HikariCP pool consistently >80% busy.

A quel punto, scaling verticale (piu' CPU/RAM su un host) tipicamente costa
piu' di scaling orizzontale (piu' istanze EMU dietro LB).

---

## Modello scelto: room-range sharding (sticky LB)

```
                client ──► Cloudflare Spectrum (anycast)
                            │
                            ▼
                    HAProxy / NGINX TCP-LB
                            │
                            │ sticky-routing per source IP (consistent hash)
                            │
                  ┌─────────┼─────────┐
                  ▼         ▼         ▼
              EMU-shard-A  EMU-shard-B  EMU-shard-C
               rooms 1-30k  rooms 30k-60k  rooms 60k-100k
                  │         │         │
                  └────┬────┴────┬────┘
                       ▼         ▼
                MariaDB primary  Redis (state shared:
                + read replica   rate-limit, session SSO)
```

Vantaggi:
  - Ogni stanza vive su UNA shard -> niente lock cross-process per item move,
    chat, trade.
  - La distribuzione e' deterministica (`shard = room_id % N`), niente
    coordinator stateful da mantenere.
  - LB sticky per IP utente -> niente reconnect storm a metà partita
    (cliente sempre allo stesso shard finche' lo "stanza id" che vuole entrare
    non cambia shard).

Svantaggi:
  - **Cross-shard friend interaction**: messenger e' globale -> serve un
    "friends router" (Redis pub/sub o broker) per consegnare DM a un user
    che e' su un'altra shard. Costo: ~1 hop Redis per DM.
  - **Cross-shard room enter**: utente in shard A clicca "entra in stanza
    X (su shard B)" -> deve disconnettersi da A e ri-connettersi a B.
    Mitigation: il "ForwardToRoomComposer" diventa "send TARGET_SHARD_IP
    al client, riconnettiti li". Cliente fa una nuova connessione.

---

## Alternativa scartata: actor-model multiplex

Idea: 1 processo "Coordinator" + N "Worker" che parlano via Akka. Ogni
stanza diventa un attore, distribuito da Akka Cluster.

Scartato perche':
  - Latency aggiuntiva 1-2ms per ogni packet (passa via attore + serialize);
  - Dipendenza Akka (~30MB JAR, learning curve);
  - Failure recovery non banale;
  - Sotto carico tipico Habbo (lots of broadcast in-room) il pattern non
    rende: Akka brilla con isolated state, non con state condiviso per
    stanza.

Il room-range sharding e' piu' "old-school" ma molto piu' semplice e
adeguato al traffic pattern.

---

## Componenti da introdurre

### 1. `ShardCoordinator` (nuovo)
Servizio leggero che mappa `room_id -> shard_addr`. Implementazione minimale:

```java
public final class ShardCoordinator {
    // Letta da config: shard.map = "shard-a:127.0.0.1:3000;shard-b:..."
    public static String shardForRoom(int roomId) {
        return SHARDS[Math.floorMod(roomId, SHARDS.length)];
    }
}
```

Nessun servizio remoto: la mappa e' statica e identica su tutti gli EMU.
Cambiare la mappa richiede restart coordinato (RFC 0001 in roadmap: live
re-shard).

### 2. `CrossShardForwardEvent` (modifica del wire protocol)

Quando il client chiede di entrare in stanza X e quella stanza vive su un
altro shard, il server invia:

```
header   = 4001 (CrossShardForward)
shardIp  = "shard-b.example.com"
shardPort = 3000
ssoTicket = "..."  (singolo-use, valido 30s, salvato in Redis)
```

Il cliente apre una nuova TCP verso `shardIp:shardPort`, fa handshake
RSA + invia il `SecureLoginEvent` con il `ssoTicket` ricevuto. Lo shard B
lo accetta come login normale.

Costo client: il nitro deve gestire "disconnect-and-reconnect transparent".
Stima: ~1 settimana di lavoro client + retro-compatibility (flag
`server.feature.cross_shard_forward`).

### 3. `MessengerRouter` (nuovo)
Per i DM cross-shard, ogni messaggio messenger pubblica su Redis pub/sub
`habbo:msg:user:<recipient_id>`. Ogni shard sottoscrive il canale per ogni
utente loggato sulla SUA shard. Cosi' un DM da shard A a utente su shard B
viaggia via Redis (1 round-trip).

### 4. `ShardHealthRouter` (nuovo)
LB esterno (HAProxy) NON conosce gli shard EMU per "intelligenza". Il
ShardHealthRouter espone `/shards-online` su ogni EMU che lista quali altri
shard rispondono. Permette al frontend (CMS) di mostrare "shard A: 1.2k
utenti, shard B: 800 utenti" e di indirizzare il sign-up alle shard meno
piene.

---

## Database

Single-primary MariaDB regge fino a ~5-10k connessioni totali se HikariCP
e' configurato bene. Tre opzioni in ordine crescente di complessita':

1. **Pool per-shard ridotto** (immediato): se prima 1 EMU usava
   `maxsize=100`, 4 shard usano `maxsize=25` ciascuna. Stesso totale.
2. **Read replica condivisa**: tutti gli shard scrivono al primary,
   leggono dalla replica (gia' supportato da Fase C).
3. **Per-shard read replica**: ogni shard ha la sua replica dedicata
   (latency piu' bassa, costo infrastruttura piu' alto).

Schema partitioning vero (PARTITION BY) **NON consigliato**: complica
backup + restore + cross-shard JOIN (friends list) e il guadagno e'
limitato per il nostro size DB (<100GB).

---

## Migration plan

1. **Step 0**: Refactor R2 (Room sync) — sharding e' inutile se ogni
   shard e' lock-bound. Vedi `ROOM_SYNC_REFACTOR.md`.
2. **Step 1**: Introduci `ShardCoordinator` (mappa statica) ma resta
   single-EMU. Test che `shardForRoom()` ritorna sempre il self.
3. **Step 2**: Implementa `CrossShardForward` lato server + client.
   Test E2E con 2 EMU sulla stessa macchina (porte diverse).
4. **Step 3**: `MessengerRouter` via Redis pub/sub.
5. **Step 4**: Deploy 2-shard production behind HAProxy. Verifica SLI
   non degradano.
6. **Step 5**: Scale to N shard secondo capacita' osservata.

**Effort stimato**: 4-6 settimane uomo, incluso client work.

---

## Quando NON shardare

- Se il bottleneck e' il **DB** non l'EMU: scalare orizzontalmente l'EMU
  moltiplica le connessioni DB. Prima ottimizza query/indici.
- Se il bottleneck e' la **rete**: sharding non aiuta se la NIC e' satura.
- Se il bottleneck e' `Room.cycle()` su poche stanze popolari: lo
  sharding distribuisce ROOM, non utenti per-room. Una stanza con 500
  utenti resta su una shard sola.

---

## Cross-shard message contracts (Redis pub/sub topology)

Tutta la comunicazione cross-shard passa per Redis pub/sub. Topology
centralizzata in una tabella unica per evitare canali fantasma e ridurre
l'attrito di debug. Tutti i payload sono JSON (Gson), con un campo `_v`
per il versioning di contratto.

### Canali

| Canale | Pattern | Publisher | Subscriber | Use case |
|---|---|---|---|---|
| `habbo:msg:user:<id>` | per-utente | shard mittente | shard del recipient | DM messenger privato |
| `habbo:friend:status` | broadcast | qualunque shard a login/logout | tutti gli shard | Notifica online/offline amici |
| `habbo:hotel:alert` | broadcast | shard di origine (mod-tool) | tutti gli shard | Hotel alert / news flash |
| `habbo:mod:ban` | broadcast | shard del moderatore | tutti gli shard | Ban applicato → kick l'utente se online altrove |
| `habbo:mod:mute` | broadcast | shard del moderatore | tutti gli shard | Mute applicato |
| `habbo:rcon:user:<id>` | per-utente | RCON Gateway | shard che ospita l'utente | give credits / alertuser / talkuser |
| `habbo:marketplace:event` | broadcast | shard che ha fatto list/buy | tutti gli shard | Refresh prezzi / nuova offerta |
| `habbo:audit:tail` | broadcast | tutti gli shard | servizio osservabilità (read-only) | Stream audit per dashboard staff |

### Schema standard

```json
{
  "_v": 1,
  "_source": "shard-A",
  "_ts": 1716578400,
  "_corr_id": "<uuid>",
  "event": "MESSENGER_DM",
  "payload": { ... }
}
```

Regole:
- `_v` viene incrementato solo per breaking change (campi nuovi al payload
  non rompono i subscriber).
- `_source` identifica lo shard mittente; **OGNI subscriber DEVE skippare i
  messaggi con `_source == self.shardId`** altrimenti si crea un loop sui
  canali broadcast.
- `_corr_id` traccia end-to-end un evento attraverso shard multipli
  (debugging + audit forensics).

---

## Operational state matrix

Dove vive ogni pezzo di stato, e con quale modello di consistenza:

| Stato | Storage | Consistenza | Note |
|---|---|---|---|
| Online users (chi è dove) | Redis `habbo:online:user:<id>` (SETEX 30s, heartbeat 10s) | Eventually consistent | SOT real-time per il routing RCON / DM |
| GameClient + Habbo (heap) | JVM in-process | Locale allo shard | Vive sullo shard del client connesso |
| Room state (utenti dentro, furni live) | JVM in-process dello shard owner | Locale allo shard owner | NON replicato: la stanza vive su 1 shard sola (design) |
| MFA staff (secret + lockout) | DB `staff_mfa` (SELECT FOR UPDATE) | Strongly consistent | Shared, single source of truth |
| MFA recovery codes | DB `staff_mfa_recovery_codes` | Strongly consistent | UPDATE atomico WHERE used_at IS NULL |
| Audit log (hash-chained) | DB `audit_log` | Strongly consistent (lock-per-chain) | Vedi sezione "Sicurezza in sharded mode" |
| Device fingerprint | DB `user_fingerprints` | Strongly consistent | INSERT con UNIQUE + count distinti |
| IP rate-limit SSO/MFA | Redis (`RedisRateLimiter`) | Eventually consistent (round-trip) | Già implementato in Fase C |
| Friend list | DB `messenger_friendships` | Strongly consistent | Letto al login, cached in heap dello shard ospitante |
| Inventory | DB `users_items` + heap | Cache-aside | Heap autoritativo in-session, flush async |
| Marketplace listings | DB | Strongly consistent | Listing/buy = transazione DB |
| Wallet (credits/pixels/points) | DB `users` + heap | Strongly consistent (atomic `UPDATE` con WHERE balance) | Vedi sezione RCON |

---

## RCON in sharded mode

Oggi RCON è single-instance (CMS → `127.0.0.1:3001`). In sharded mode
l'utente target può essere su qualunque shard. Due opzioni:

### Opzione A — RCON Gateway (default scelto)

Un solo RCONServer (`gateway`) riceve dal CMS, individua l'utente via
Redis (`habbo:online:user:<id> = <shardId>`), pubblica
`habbo:rcon:user:<id>` su Redis. Lo shard che possiede l'utente esegue
il comando localmente.

```
CMS ──► RCON Gateway ──► Redis pub habbo:rcon:user:42
                                       │
                          ┌────────────┴────────────┐
                          ▼                         ▼
                  shard A (ospita user 42)    shard B (skip: _source filter)
                          │
                          ▼
                  GameClient.handle(...)
```

**Pro**: il CMS non sa nulla di sharding (zero modifiche `Rcon.php` /
`sendMUS`).
**Contro**: ~1 ms di latenza extra Redis (LAN); il gateway è SPOF mitigato
da 2 istanze attive con allow-list IP del CMS.
**Nota**: il token RCON + anti-replay (già implementati) si applicano al
gateway. Il nonce cache va spostata su Redis se il gateway è in HA-pair
(oggi `seenNonces` è `ConcurrentHashMap` in-process).

### Opzione B — Smart CMS

Il CMS chiede `/shards-online` per scoprire dove sta l'utente, poi si
connette al RCON dello shard direttamente. Più veloce ma richiede
modifiche a `Rcon.php` e a tutti i `sendMUS`.

**Scelta**: A nel primo round (zero modifiche CMS). B in roadmap se la
latenza Redis diventa misurabile.

---

## Failure modes

### Shard crash
- Tutti gli utenti sullo shard subiscono TCP RST.
- LB rileva via `/health` (timeout 5s) e marca DOWN.
- Gli utenti riconnettono → LB ri-hash su shard alive.
- **Cosa va perso**: room state non flushato (utenti dentro la stanza,
  furniture pending). `RoomManager.persistAsync` può perdere una finestra
  max ~5s di scritture.
- Audit log: nessuna perdita (write sincrono).

### Redis down
- Cross-shard messaging si interrompe (DM non recapitati, broadcast non
  visti).
- `RedisRateLimiter` cade su fallback in-memory locale → un attaccante
  brute-force ha M volte più tentativi (M = numero shard) finché Redis non
  torna.
- **Mitigation**: `redis.fail_closed_seconds` (config) → se Redis è giù
  per più di N secondi, login rifiuta nuove connessioni (fail closed) per
  proteggere il rate-limit.

### LB (HAProxy) down
- Catastrofico finché non c'è failover.
- Cloudflare Spectrum NON sa parlare con N shard direttamente — l'LB DEVE
  esistere.
- **Soluzioni**:
  - HAProxy in HA-pair con keepalived/VRRP su 2 host.
  - O 2 Spectrum app verso 2 LB con DNS round-robin lato Cloudflare.

### Split-brain (network partition fra shard e Redis)
- Shard A vede Redis ma non shard B (o viceversa).
- Senza coordination, due shard potrebbero credere di possedere lo stesso
  utente.
- **Mitigation**: Redis è la SOT per "chi è dove". Ogni shard fa heartbeat
  `SETEX habbo:online:user:<id> <self> 30`. Quando A nota `online:user:42
  != self` durante un evento locale, chiude la connessione locale
  ("you're double-logged-in").
- Se Redis è giù è il caso "Redis down" (fail closed).

### Shard "drift" — utente entra in una stanza di un altro shard
- Atteso dal design (room-range sharding).
- Vedi `CrossShardForward`: il client riceve `(shard_ip, sso_ticket)`,
  apre nuova TCP, fa login. La vecchia connessione si chiude.
- **Race**: durante i ~200ms di handoff l'utente è "in transito".
  Mitigation: Redis `habbo:online:user:<id>` ha TTL 30s e il TARGET shard
  fa overwrite all'arrivo. DM ricevuti nel mentre vanno persi (best-effort
  by design — il client può fare retry).

---

## Observability — metriche per shard

Ogni shard espone (estendendo `HealthEndpoint` già esistente):

| Metric | Tipo | Soglia warn / crit |
|---|---|---|
| `shard.id` | label | — |
| `connections.active` | gauge | > 4000 / > 5000 |
| `connections.peak_24h` | gauge | per capacity planning |
| `packet.latency.p50/p95/p99` | histogram (`LatencyHistogram` già esiste) | p99 > 100ms / > 250ms |
| `redis.publish.errors_per_sec` | counter | > 1 / > 10 |
| `redis.subscribe.lag_ms` | gauge | > 100ms / > 500ms |
| `db.pool.busy_pct` | gauge (HikariCP MBean) | > 70% / > 90% |
| `room.cycle.duration_ms.p99` | histogram | > 50ms / > 200ms (target tick 500ms) |
| `cross_shard.forward.rate` | counter | rate di handoff (info, non alert) |
| `cross_shard.dm.delivered/dropped` | counter | dropped > 0 sustained = bug |
| `mfa.active_elevated_sessions` | gauge | sanity check |
| `audit.write.rate_per_sec` | counter | spike = possibile abuse |

Dashboard consigliata: una riga per shard, colonne con le metriche;
overlay totale hotel-wide. Alert via `AlertSink` (webhook già implementato)
su soglie critical → Discord/Slack canale ops.

---

## Rolling restart procedure

Aggiornare uno shard senza disconnessione percepibile:

1. **Drain**: chiama `POST /admin/shard/<id>/drain` → lo shard ritorna 503
   su `/health` (LB lo esclude dai nuovi accept).
2. **Re-route nuovi utenti**: il LB hash-source non manda più SYN allo
   shard draining. Utenti già connessi restano.
3. **Soft handoff stanze**: lo shard draining può preventivamente
   redistribuire le sue stanze tramite `CrossShardForward` verso shard
   peer (best-effort; gli utenti senza forward restano connessi fino
   alla loro naturale disconnect).
4. **Attesa**: 30-60s perché le sessioni si chiudano naturalmente.
5. `kill -TERM` → shutdown graceful (chiude `ChannelGroup`, flush DB
   async, `DEL habbo:online:user:*` per i propri).
6. Restart con nuova versione.
7. `/health` torna 200 → LB reincluide.
8. Ripeti per ogni shard.

**Tempo totale per N shard**: ~N × 90s. Zero downtime percepito per gli
utenti se l'hotel ha > 1 shard (altrimenti = downtime di N × 90s).

---

## Sicurezza in sharded mode

- **MFA staff**: stato in DB `staff_mfa` (locked_until, fail_count). Ogni
  shard fa `SELECT FOR UPDATE` → un attaccante NON può "saltare shard"
  per resettare il lockout.
- **Recovery codes**: UPDATE atomico WHERE used_at IS NULL — un codice
  consumato simultaneamente da due shard fallisce su uno dei due
  (affected=0).
- **Audit log (hash-chained)**: la chain HMAC richiede UN writer alla
  volta per essere verificabile. Due strategie:
  - **Per-shard chain** (default): tabella `audit_log` ha colonna
    `shard_id`; la chain è `audit_log WHERE shard_id = ?`. Verifica per
    shard, ricostruzione globale via tool offline. Latency ottima.
  - **Chain monolitica con lock Redis**: tutti gli shard si serializzano
    su `SET NX habbo:audit:lock`. Latency peggiore (ogni write fa una
    network roundtrip), forensics più semplice.
  - **Decisione**: per-shard. Aggiungere `shard_id INT NOT NULL` al
    momento della migration.
- **RCON token + anti-replay**: invariato a livello logico. Il gateway
  applica entrambi. Il nonce cache va condivisa su Redis se il gateway
  è in HA-pair (oggi è `ConcurrentHashMap` in-process — single point).
- **AlertSink webhook**: ogni shard pubblica indipendentemente. Il
  ricevitore (Discord) deve essere idempotente o accettare duplicati
  durante eventi cross-shard (es. MULTIACCOUNT_DETECTED triggerato dal
  pub/sub potrebbe arrivare da più shard).

---

## Open questions / decisioni da prendere

| # | Domanda | Default proposto |
|---|---|---|
| 1 | Audit chain per-shard o monolitica? | **Per-shard** (latency, ricostruzione offline) |
| 2 | Numero iniziale di shard? | **2** (validare il modello, poi 4-8) |
| 3 | Sticky LB per source IP o per SSO ticket? | **Source IP** (HAProxy hash-source, semplice; accetto churn marginale su mobile carrier NAT) |
| 4 | Mappa shard statica o dinamica? | **Statica** (config). Live re-shard = RFC futuro |
| 5 | WS bridge per-shard o centralizzato? | **Per-shard** (replica statica del plugin NitroWebsockets) |
| 6 | Geo-sharding (region affinity)? | **Out of scope** finché tutto il traffico è IT |
| 7 | RCON gateway in HA-pair? | **Sì in produzione**, single in test (nonce cache su Redis quando HA) |

---

## Implementation roadmap (concreta)

| Fase | Effort | Cosa | Validazione |
|---|---|---|---|
| **0** | 1 giorno | `ShardCoordinator` (mappa statica). In single-EMU ritorna sempre `self`. | Unit test |
| **1** | 3-5 giorni | Redis pub/sub topology: `MessengerRouter` + `OnlinePresence` (heartbeat SETEX) | E2E 2 EMU su porte diverse |
| **2** | 1 settimana | `CrossShardForward` packet + client support nel ws-bridge plugin | Integration test col client Nitro |
| **3** | 3-5 giorni | RCON Gateway con pub/sub | Manual test: API alertuser cross-shard |
| **4** | 3-5 giorni | Drain mode + rolling restart procedure | Chaos test (kill shard sotto carico) |
| **5** | continuo | Deploy 2-shard in produzione, scale a 4-8 secondo i grafici | SLI monitoring + alert |

**Total effort**: 4-6 settimane uomo, divisibili tra server + client + ops
in sprint paralleli.

---

## Cosa NON e' in questo doc (out of scope first round)

- **Geo-sharding** (region affinity, multi-DC).
- **Live re-sharding** (cambiare mappa room→shard senza restart).
- **Multi-DB sharding** (DB diversi per shard diversi).
- **Federazione tra hotel indipendenti** (futuristico, non scaling).

Ognuno di questi è un design separato. Ne parliamo se mai diventeranno
una necessità reale (probabilmente mai per un retro Habbo).
