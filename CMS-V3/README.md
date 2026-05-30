# Habboproject CMS-V3

Nuovo CMS basato su React 19 + Hono (Node 22). Replica visivamente
habbo.it ufficiale, con focus su **cybersecurity da production**:

- **Auth**: Argon2id passwords + JWT access (15 min) + refresh token in
  httpOnly Secure cookie, rotazione automatica, revoca via DB
- **Headers**: HSTS preload, CSP strict con nonce, X-Frame-Options DENY,
  Referrer-Policy strict-origin-when-cross-origin, X-Content-Type-Options nosniff
- **CSRF**: double-submit cookie + SameSite=Lax (default su tutti i cookie)
- **Rate limiting**: sliding window per IP/endpoint (auth login 5/min,
  generic API 100/min)
- **SQL injection**: solo prepared statements via mysql2 (parametrizzati)
- **XSS**: React auto-encodes; DOMPurify per HTML utente (motto, descrizioni)
- **2FA TOTP**: già supportato dal DB `staff_mfa` per staff (riusato per /v3)
- **Password breach check**: HaveIBeenPwned API k-anonymity sul register
- **Audit log**: tutte le azioni sensibili (mod, admin) loggate in `audit_log`
- **Rotazione SSO**: ticket Nitro generato per-richiesta, scade in 60s

## Architettura

```
CMS-V3/
├── apps/
│   ├── api/          # Hono + TypeScript — REST API JSON
│   │   src/
│   │     index.ts        # entry, port 8092
│   │     middleware/     # auth, csrf, rate-limit, helmet, audit
│   │     routes/         # /auth, /me, /news, /rooms, /oracolo, ...
│   │     services/       # business logic
│   │     db/             # mysql2 pool + repositories (parameterized)
│   │     security/       # argon2, jwt, csrf, totp helpers
│   │     env.ts          # zod-validated env vars
│   └── web/          # React 19 + Vite — frontend, port 8090
│       src/
│         App.tsx
│         routes/         # React Router 7 route components
│         components/     # NavBar, Card, Button, ... (consume ui-kit)
│         api/            # fetch wrappers /api/v2/*
│         hooks/          # useAuth, useUserData, ...
│         styles/         # Tailwind + design tokens
│         i18n/           # IT
└── packages/
    ├── design-tokens/    # palette + tipografia + spacing (TS const)
    └── ui-kit/           # componenti React condivisi
```

## Porte (strangler pattern)

| Servizio | Porta | Stato |
|---|---|---|
| CMS legacy PHP (vecchio) | **8080** | Attivo, non toccato |
| Nitro-V3 client (in CMS legacy) | 8080/react/ | Attivo |
| EMU Game Server | 3000 | Attivo |
| EMU Nitro WebSocket | 38820 | Attivo |
| **CMS-V3 web** (nuovo) | **8090** | In sviluppo |
| **CMS-V3 API** (nuovo) | **8092** | In sviluppo |
| MariaDB | 3306 | Attivo (DB `ms`) |

Il vecchio CMS PHP resta su `:8080` fino al cutover finale.

## Comandi

```sh
# Setup
cd CMS-V3
yarn install

# Sviluppo (api + web in parallelo)
yarn dev

# Build production
yarn build

# Test + lint + typecheck
yarn test
yarn lint
yarn typecheck
```

## Variabili d'ambiente (`apps/api/.env`)

Vedi `apps/api/.env.example` per la lista completa con default sicuri.
**Generare** `JWT_SECRET` e `REFRESH_TOKEN_SECRET` con `openssl rand -hex 64`.

## Database

CMS-V3 condivide il DB `ms` con EMU e vecchio CMS. Le NUOVE tabelle che
introduce hanno prefisso `cms_v3_`:

- `cms_v3_refresh_tokens` (token rotation + revoke)
- `cms_v3_audit_log` (eventi sensibili)
- `cms_v3_rate_limits` (sliding window state)

Schema in `apps/api/src/db/migrations/`.

## Roadmap

- [x] Fase 1: scaffold + design tokens + UI kit base + Home/Login
- [ ] Fase 2: Profile, Settings, Community, Shop, Oracolo bacheca
- [ ] Fase 3: housekeeping admin + cutover finale
