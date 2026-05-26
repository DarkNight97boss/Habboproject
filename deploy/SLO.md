# Service Level Objectives — Habboproject EMU

Documento contrattuale tra operations e prodotto. Definisce **cosa significa
"il server funziona"** in termini misurabili dalle metriche Prometheus
gia' esposte da `HealthEndpoint`.

Approccio: 4 SLI critici, ognuno con target SLO + error budget mensile.
Per ciascuno, indichiamo come e' misurato (PromQL) e cosa succede quando
sfori il budget.

---

## SLI 1 — Availability

**Definizione**: il game server risponde a `/readyz` con 200 OK.

**SLI** (success ratio in window):
```promql
avg_over_time(up{job="habbo-emu"}[30d])
```

**SLO**: **99.5%** uptime mensile = **3.6h** downtime/mese budgetate.

**Cosa fai se sfori**:
- Stop ai feature deploy fino a fine mese (focus su stabilita').
- Post-mortem ad ogni incident >5 min.

**Misurazione**: `EmuDown` alert critical (vedi `prometheus_rules/habbo_alerts.yml`).

---

## SLI 2 — Sessioni stabili

**Definizione**: gli utenti online NON crollano di colpo (no mass-disconnect).

**SLI** (% del tempo in cui il drop in 5 min e' <=10%):
```promql
1 - (
  count_over_time(
    (
      (habbo_users_online - habbo_users_online offset 5m)
      / habbo_users_online offset 5m < -0.10
    )[30d:5m]
  )
  /
  count_over_time(habbo_users_online[30d:5m])
)
```

**SLO**: **99.0%** del tempo (= max 30 finestre 5-min con drop >10% al mese).

**Cosa misuriamo**: un crollo improvviso del 30%+ indica bug grave (es. wave packet difettoso). Il 10% ammette movimenti normali pomeridiani/serali.

**Alert**: `SuddenUserDrop` (warning), threshold -30% — vedi `habbo_alerts.yml`.

---

## SLI 3 — Latency packet round-trip

**Definizione**: tempo da arrivo packet a invio risposta < soglia.

**TODO**: questo SLI non e' ancora misurato (non esponiamo histogram di
processing time). Per esporlo in Fase D:
1. Wrap `PacketManager.handlePacket()` in un `Timer` Micrometer.
2. Espone `habbo_packet_processing_seconds_bucket{quantile=...}`.
3. SLO target: **p99 < 100ms** per pacchetti senza-DB; **p99 < 500ms** per
   pacchetti con-DB.

Per ora il proxy SLI: `habbo_jvm_threads_active` stabile (no thread starvation)
+ p95 connect time < 50ms da `tools/loadtest` (eseguito settimanalmente).

---

## SLI 4 — Anti-abuse efficacy

**Definizione**: percentuale di packet "sani" che NON vengono ratelimitati.

**SLI**:
```promql
1 - (
  rate(habbo_ratelimit_hits_total[5m])
  /
  (rate(habbo_ratelimit_hits_total[5m]) + rate(habbo_packets_rejected_total[5m]) + 1)
)
```
(il "+1" evita divisioni per zero quando non c'e' traffico)

**SLO**: **>= 99%** dei pacchetti accettati (= ratelimit fa il suo, ma non
e' iperaggressivo).

**Trigger**:
- SLI < 99% per >10 min => ratelimit floor (`sec.ratelimit.default.ms`)
  forse troppo alto, oppure attacco DDoS in corso (verifica `PacketsRejectedStorm`).

---

## Error budget policy

Quando l'error budget di un SLI e' **esaurito** prima di fine mese:

| Stato budget | Cosa cambia |
|---|---|
| Burned <50% | Tutto normale: deploy feature OK. |
| Burned 50-100% | Deploy NON urgenti rimandati a inizio mese successivo. |
| Burned >100% | Solo fix + improvement di stabilita'. Niente nuove feature. |

Tale policy va revisitata trimestralmente.

---

## Come consultare gli SLI live

Una volta avviato `docker compose --profile obs up -d`:

  Grafana dashboard:  http://127.0.0.1:3030
    Habboproject -> Overview (panel SLI dedicato in roadmap Fase D)

  Prometheus query manuale:
    http://127.0.0.1:9091/graph

  Active alerts:
    http://127.0.0.1:9093/

---

## Roadmap futura (non in scope ora)

- [ ] Histogram processing-time per SLI 3 (Micrometer + Prometheus client)
- [ ] Aggiunta Grafana panel "SLO burn rate" con `slo_burn_rate` ricetta SRE
- [ ] Long-term storage Thanos / Mimir per analisi annuali
- [ ] Synthetic monitoring (canary che fa login + invia 1 chat ogni 60s)
