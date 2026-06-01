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

## Ambiente di Sviluppo (dev.asteriacore.online)

[![Dev Deploy](https://github.com/DarkNight97boss/Habboproject/actions/workflows/pr-dev-deploy.yml/badge.svg)](https://github.com/DarkNight97boss/Habboproject/actions/workflows/pr-dev-deploy.yml)
[![Prod Deploy](https://github.com/DarkNight97boss/Habboproject/actions/workflows/prod-deploy.yml/badge.svg?branch=main)](https://github.com/DarkNight97boss/Habboproject/actions/workflows/prod-deploy.yml)

`dev.asteriacore.online` è l'**ambiente di anteprima** (preview) di Asteria
Core. Ogni Pull Request aperta verso `main` da un autore in whitelist viene
automaticamente buildata su GitHub Actions e deployata sul VPS Hetzner, dove
gira **in parallelo** alla produzione (stessa macchina, servizi systemd
separati, database `ms_dev` distinto, porte dedicate). Il deploy include
CMS-V3 (web + API Hono), EMU Java e — se la PR tocca `CMS/react/**` o
`Nitro-V3/**` — anche il bundle Nitro su un bucket R2 dedicato.

L'obiettivo è che ogni revisore possa aprire `https://dev.asteriacore.online`,
fare login, premere **GIOCA** e testare la gameroom end-to-end **prima** del
merge. Niente Docker, niente secondo VPS: un solo ambiente dev riusato per
ogni PR attiva (concurrency-group `dev`, cancel-in-progress).

Al merge su `main`, un secondo workflow (`prod-deploy.yml`) ricompila e
promuove gli artifact su `asteriacore.online`.

| Ambiente | URL                                                | Branch / Trigger                | Database  | Workflow              | Servizi systemd                  |
|----------|----------------------------------------------------|---------------------------------|-----------|-----------------------|----------------------------------|
| **dev**  | https://dev.asteriacore.online                     | PR aperta verso `main`          | `ms_dev`  | `pr-dev-deploy.yml`   | `habbo-emu-dev`, `cms-api-dev`   |
| **prod** | https://asteriacore.online                         | push / merge su `main`          | `ms`      | `prod-deploy.yml`     | `habbo-emu`, `cms-api`           |

Endpoint ausiliari dev: `dev-api.asteriacore.online` (CMS-V3 API Hono),
`dev-hotel.asteriacore.online` (gameroom Nitro). Asset client serviti da
bucket R2 `asteria-gamedata-dev` (mirror del prod `asteria-gamedata`).

Per dettagli su workflow GitHub Actions, secrets richiesti, allowlist autori,
script `apply-dev.sh` / `apply-sql.sh` e procedura di rollback, vedi
[`CONTRIBUTING.md`](CONTRIBUTING.md).

## Licenza

GPL-3.0 (ereditata da Arcturus Morningstar). Vedi `EMU/LICENSE`.
