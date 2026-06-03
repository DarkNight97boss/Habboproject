# Asteria Core

> **Deploy automatico:** ogni push a `main` che tocca `EMU/**` ricostruisce il jar
> (Maven) e lo ridispiega in produzione via `.github/workflows/prod-deploy.yml` —
> con backup pre-deploy, restart `habbo-emu` (broadcast in-game) e smoke-test su
> `:2096` + rollback automatico se l'EMU non riparte.

**Asteria Core** è un server di gioco real-time per ambienti virtuali, derivato da
[Arcturus Morningstar](https://git.krews.org/morningstar/Arcturus-Community) di
TheGeneral. Distribuito sotto licenza
[GNU General Public License v3](https://www.gnu.org/licenses/gpl-3.0.txt) come
prodotto educativo open-source.

Mantiene compatibilità con i client Habbo-protocol esistenti (Nitro raccomandato)
e con lo schema database upstream — il rebrand riguarda il prodotto derivato,
non rompe nulla a livello tecnico.

| Client supportato | Note |
| ----------------- | ---- |
| [Nitro (raccomandato)](https://github.com/billsonnn/nitro-react) | Richiede il plugin [ms-websockets](https://git.krews.org/nitro/ms-websockets/-/releases) per il bridge WebSocket → TCP. |
| Flash legacy `PRODUCTION-201611291003-338511768` | Solo per setup legacy; non più mantenuto. |

## Caratteristiche di Asteria Core (rispetto al fork upstream)

Vedi `deploy/SLO.md`, `deploy/ROOM_SYNC_REFACTOR.md`, `deploy/SHARDING_DESIGN.md`
per i dettagli architetturali. In sintesi:

- **Sicurezza applicativa hardened**: 20+ wave di audit + 8 CVE chiuse (Netty,
  jsoup, logback, gson, MySQL connector, PHP-CGI).
- **MFA staff** TOTP Google Authenticator constant-time + lockout brute-force.
- **Audit log tamper-evident** HMAC-SHA-256 hash-chain.
- **Anti-flood / anti-Tanji** integrati (`ChatSpamGuard`,
  `UnknownPacketGuard`, `IpRateLimiter`).
- **Observability**: HealthEndpoint `/metrics` Prometheus + 11 alert PromQL +
  4 SLI definiti.
- **Multi-host ready**: Redis cross-instance rate-limit, MariaDB read replica
  HikariCP pool dedicato.
- **Docker production-ready**: multi-stage Dockerfile, non-root, healthcheck,
  graceful shutdown drain.
- **Test automatici**: JUnit 5 + Testcontainers (29 unit + 3 integration).
- **CI/CD**: Dependabot + CodeQL workflow.

## Build

```bash
cd EMU
mvn -B -DskipTests package
# output: target/Habbo-3.5.5-jar-with-dependencies.jar
```

Il filename del JAR mantiene il prefisso `Habbo-*` per non rompere gli script
di deploy esistenti (`start_clean_stack.ps1`, Dockerfile). È un'etichetta
tecnica, non branding utente.

## Esecuzione

```bash
# Stack di sviluppo (host network, MariaDB su localhost)
docker compose up -d

# Stack production-like (Cloudflare-fronted, Redis, observability)
docker compose --profile redis --profile obs up -d
```

Vedi `SETUP.md` per il setup completo da zero (DB, config, client Nitro,
porte, CMS).

## Contribuire

Bug report e patch sono benvenuti. Apri un issue / PR sul repository:
`https://github.com/DarkNight97boss/Habboproject`.

Prima di mandare codice:
- esegui `mvn test` (29 test devono passare)
- niente push diretto su `main` senza review (Dependabot + CodeQL gate)

Per modifiche upstream (Arcturus Morningstar), riferisciti al loro
[issue tracker](https://git.krews.org/morningstar/Arcturus-Community/issues).

## Licenza & attribution

Codice originale di Arcturus: TheGeneral. Vedi `LICENSE` (GPL-3.0) e i
copyright header nei file sorgente.

I crediti dei contributori upstream sono mantenuti sotto. Aggiungi il tuo se
contribuisci ad Asteria Core.

### Crediti upstream Arcturus Morningstar

       - TheGeneral (Arcturus Emulator)
       - Beny, Alejandro, Capheus, Skeletor, Harmonic, Mike, Remco, zGrav,
         Quadral, Harmony, Swirny, ArpyAge, Mikkel, Rodolfo, Rasmus,
         Kitt Mustang, Snaiker, nttzx, necmi, Dome, Jose Flores, Cam,
         Oliver, Narzo, Tenshie, MartenM, Ridge, SenpaiDipper, Thijmen,
         Brenoepic, Stankman, Laynester, Yordi

### Contributori Asteria Core

       - DarkNight97boss (maintainer)
