# Asteria Core

Stack completo per un ambiente virtuale real-time (server di gioco + CMS web +
API + client Nitro), basato sul motore [Arcturus Morningstar](EMU/README.md)
in versione ribrandizzata "Asteria Core".

## Struttura repository

```
├── EMU/                Server di gioco (Java 8 target, JRE 11+, Maven)
├── CMS/                Pannello web utenti + amministrativo (PHP)
├── API/                Microservizio HTTP per audio / RCON / extra
├── client-patches/     Patch source-level per il client Nitro
├── deploy/             Caddyfile, Prometheus, Grafana, Alertmanager
├── tools/              Utility: gen_rcon_token, loadtest, ecc.
├── docker-compose.yml  Stack containerizzato (profili: redis, obs, backup)
└── start_clean_stack.ps1  Avvio dev su Windows (XAMPP + Java + Nitro)
```

## Avvio rapido (sviluppo, Windows)

```powershell
.\start_clean_stack.ps1
# poi apri: http://localhost:8090/index.html?sso=<auth_ticket>&room=<id>
```

## Avvio rapido (produzione, Docker)

```bash
cp .env.example .env
nano .env                 # popola DB password, HMAC key, Redis password
cp EMU/config.ini.example EMU/config.ini
nano EMU/config.ini       # popola db/rcon.token/audit.signing.key

docker compose build
docker compose --profile redis --profile obs up -d
```

## Documentazione

- [`EMU/README.md`](EMU/README.md) — server di gioco Asteria Core
- [`SETUP.md`](SETUP.md) — setup completo da zero
- [`deploy/SLO.md`](deploy/SLO.md) — Service Level Objectives + alerting
- [`deploy/ROOM_SYNC_REFACTOR.md`](deploy/ROOM_SYNC_REFACTOR.md) — roadmap performance
- [`deploy/SHARDING_DESIGN.md`](deploy/SHARDING_DESIGN.md) — strategia multi-shard

## Stato

- ✅ Sicurezza applicativa hardened (20+ audit waves, 8 CVE chiuse, MFA TOTP)
- ✅ Test automatici (29 unit + 3 integration con Testcontainers)
- ✅ Docker production-ready (multi-stage, non-root, healthcheck)
- ✅ Observability completa (Prometheus + Grafana + 11 alert PromQL)
- ✅ Multi-host ready (Redis + read replica)

## Deploy (asteriacore.online)

[![Prod Deploy](https://github.com/DarkNight97boss/Habboproject/actions/workflows/prod-deploy.yml/badge.svg?branch=main)](https://github.com/DarkNight97boss/Habboproject/actions/workflows/prod-deploy.yml)

Ogni Pull Request verso `main` passa i quality gate di `ci.yml` (typecheck,
lint, test, build). Al merge su `main`, il workflow `prod-deploy.yml` ricompila
gli artifact (CMS-V3 web + API Hono, EMU Java, SQL, Nitro) e li promuove sul
VPS Hetzner con backup pre-deploy e rollback.

| Ambiente | URL                        | Trigger                 | Database | Workflow          | Servizi systemd        |
|----------|----------------------------|-------------------------|----------|-------------------|------------------------|
| **prod** | https://asteriacore.online | push / merge su `main`  | `ms`     | `prod-deploy.yml` | `habbo-emu`, `cms-api` |

Endpoint ausiliari: `api.asteriacore.online` (CMS-V3 API Hono),
`hotel.asteriacore.online` (gameroom Nitro). Asset client serviti dal bucket
R2 `asteria-gamedata`.

Per dettagli su workflow GitHub Actions, secrets richiesti, allowlist autori,
script `apply-prod.sh` / `apply-sql.sh` e procedura di rollback, vedi
[`CONTRIBUTING.md`](CONTRIBUTING.md).

## Licenza

GPL-3.0 (ereditata da Arcturus Morningstar). Vedi `EMU/LICENSE`.
