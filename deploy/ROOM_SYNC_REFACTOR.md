# Room sync refactor — design doc

Stato: **roadmap**, non implementato. Documento per chi prendera' in mano
il refactor (item R2 nel deliverable Senior Java Security audit).

---

## Perche' serve

`EMU/src/main/java/com/eu/habbo/habbohotel/rooms/Room.java` e' **5434 righe**
con **>30 blocchi `synchronized`** intrecciati su 6 lock diversi
(`loadLock`, `roomUnitLock`, `activeTrades`, `mutedHabbos`, `roomItems`,
`habboQueue`).

Sotto carico (>200 utenti per stanza) il tick di sala diventa serializzato:
ogni `Room.cycle()` deve prendere il lock principale, scorrere `currentHabbos`,
broadcastare composer, scorrere `roomItems` per il movement -> tutti
operatori O(N×M) sotto sync(this).

Risultato osservato (stima da CodeQL + grep):
  - Tick latency cresce linearmente con utenti per stanza.
  - SLI 3 latency p99 sfora 500ms a partire da ~150 utenti/stanza.
  - Contention CPU "wasted on locks" >40% sotto picco.

---

## Cosa c'e' ora (anti-pattern noti)

1. **Lock-on-this** implicito (`public synchronized void X()`) su 8 metodi
   pubblici. Significa che chiunque sincronizzi sul `Room` esterno blocca
   anche i tick.

2. **Lock annidato**: `synchronized(loadLock)` poi `synchronized(roomUnitLock)`
   poi `synchronized(roomItems)`. Ordine non documentato — alto rischio
   deadlock se qualcuno inverte i livelli.

3. **Iterazione con modifica concorrente**: `currentBots.forEachValue(...)`
   senza copia. Se un bot viene rimosso durante il tick si beccava
   `ConcurrentModificationException` (mitigato in passato con TCollections
   synchronized wrapper, ma resta fragile).

4. **`TCollections.synchronizedMap()`**: API anni 2010, blocca su ogni read.
   Le moderne `ConcurrentHashMap` permettono N reader senza lock + 1 writer.

---

## Target architecture

### Sostituire i 3 lock principali con strutture concurrent-friendly:

| Lock attuale | Sostituto | Motivazione |
|---|---|---|
| `synchronized(roomItems)` | `ConcurrentHashMap<Integer,HabboItem>` | Lookup-by-id e' il pattern dominante; CHM ha read O(1) lock-free. |
| `synchronized(habboQueue)` | `ConcurrentHashMap<Integer,Habbo>` + snapshot per iterate | Broadcast itera sul valueSet -> snapshot via `values().toArray()`. |
| `synchronized(loadLock)` | `StampedLock` con read optimistic | Load e' raro (boot stanza), tick scorre in read-mode. |

### Iteration pattern

Sostituire i `forEachValue` su collezione live con snapshot one-shot:

```java
// PRIMA
synchronized (this.habboQueue) {
    for (Habbo h : habboQueue.valueCollection()) {
        h.send(composer);
    }
}

// DOPO
Habbo[] snapshot = habboQueue.values().toArray(new Habbo[0]);
for (Habbo h : snapshot) {
    h.send(composer);
}
```

Costo aggiuntivo: una `Object[]` alloc per broadcast. Vale la pena: il broadcast non blocca piu' add/remove concorrenti.

### Tick `cycle()` lock-free

L'unico path che richiede ANCORA il lock e' la mutazione coordinata di
state interno (es. "tutti i bot avanzano simultaneamente"). Per quel
caso uso `StampedLock.writeLock()`. Tutti gli altri tick path (broadcast,
movement read, item lookup) usano read-optimistic.

---

## Migration plan

1. **Step 0 — Coverage**: scrivi test che esercitano (a) cycle()
   sotto traffico, (b) join/leave concorrenti, (c) trade concorrente con
   inventory mutation. Senza questi test, il refactor e' suicidio.
   Strumento: Testcontainers + EMU embed in-process (richiede `Emulator.startEmbedded()`
   factory che oggi non c'e' — va aggiunto).

2. **Step 1 — Estrai stato pubblico in mini-classi immutabili**:
   `RoomSnapshot` (lista habbo, lista bot, lista item) viene rebuildato
   dal cycle, e i lettori esterni leggono SOLO il snapshot. Hot path
   diventa lock-free.

3. **Step 2 — Sostituisci roomItems**: prima collezione da migrare,
   pochi accessi mutativi, alto guadagno.

4. **Step 3 — Sostituisci habboQueue/currentBots**: collezioni piu' calde,
   serve attenzione su modifica concorrente nei broadcast.

5. **Step 4 — StampedLock su `loadLock`**: read-optimistic per il tick,
   write lock per load/unload.

6. **Step 5 — Rimuovi `public synchronized` dai metodi pubblici**:
   se nessuno fuori dovesse aver dipeso del lock-on-this, niente cambia.
   Verificare con `git grep "synchronized.*Room"`.

---

## Rischi & rollback

- **Rischio principale**: deadlock latenti emergono solo sotto carico.
  Mitigation: load test con `tools/loadtest` su 2000 client + JMH
  micro-benchmark prima del rollout.
- **Rollback**: ogni step deve essere su un branch dedicato, mergiabile
  indipendentemente, con feature flag (`room.sync.refactor.enabled`)
  per disattivarlo a runtime se in prod va male.

---

## Effort stimato

- Step 0 (coverage):     **3-5 giorni** (richiede Emulator.startEmbedded refactor)
- Step 1 (snapshot):     **2 giorni**
- Step 2 (roomItems):    **1 giorno** + test
- Step 3 (habboQueue):   **2 giorni** + load test
- Step 4 (StampedLock):  **2 giorni** + chaos test
- Step 5 (cleanup):      **1 giorno**

**Totale**: ~2 settimane uomo, con load test continuo.

## Target di guadagno

- Tick latency p99 a 200 utenti/stanza: **da ~600ms a <100ms**.
- CPU "wasted on locks": **da >40% a <5%**.
- Max utenti per stanza prima di soglia SLO: **da ~200 a ~1000**.
