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
