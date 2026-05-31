# CMS-V3 — Security Posture

Questo documento descrive il modello di sicurezza del nuovo CMS.

## Threat model

Attori considerati:
- **Utente anonimo malevolo** (script kiddies, bot)
- **Utente autenticato malevolo** (privilege escalation, abuse APIs)
- **Sniffing/MITM** (TLS strip, downgrade attacks)
- **Furto credenziali** (phishing, password reuse, token theft)
- **Insider** (staff con privilegi, audit log integrity)

Fuori scope (per ora):
- Compromissione fisica del server
- Side-channel attacks (timing, cache)
- Supply chain compromise (npm packages — mitigato con `yarn audit` periodico)

## Difese implementate

### Authentication
- **bcrypt cost 12** per password hashing (OWASP 2024 min cost 10) — vincolo VARCHAR(64) nella tabella `users` (Argon2id ~96char overflowa, vedi memory `cms-v3-pwhash-overflow`)
- **JWT access token** HS256, vita **15 min**, claims: sub, username, rank, jti, iss, aud
- **Refresh token** HS256 con secret SEPARATO da JWT, vita **14 giorni**, salvato in DB
- **Rotation** automatica refresh: ad ogni uso emette nuovo + invalida vecchio
- **Reuse detection**: se un refresh token già "used_at" viene ripresentato → revoca dell'**intera family** (signal di furto)
- **verifyPassword multi-formato**: legge bcrypt (`$2*$`), Argon2id (`$argon2*`), SHA-512 raw (128 hex) — zero-downtime per migrazioni future

### Cookies
- `cms_v3_access` e `cms_v3_refresh`: **HttpOnly**, **Secure** (in prod), **SameSite=Lax**
- Path=/, Max-Age esplicito, no Domain leak

### CSRF (parziale)
- **NON enforced** al momento (env definito ma middleware mancante). Mitigazioni attuali:
  - **CORS whitelist** esplicita su `ALLOWED_ORIGINS` (no wildcard)
  - **SameSite=Lax** sui cookie auth → form/img cross-site NON inviano cookie
  - **Content-Type: application/json** su PATCH/POST → preflight CORS richiesto → origini fuori whitelist bloccate dal browser
- **Verdetto**: rischio basso per uso normale, ma defense-in-depth merita middleware double-submit cookie. TODO pre-deploy pubblico.

### Rate limiting
- Sliding window in-memory (per ora — produzione: Redis)
- `/auth/login`: 5/min per IP
- `/auth/register`: 3/ora per IP
- Generic API: 100/min per IP

### Input validation
- **Zod** schemas su tutti i payload incoming
- Type narrowing + denylist via regex per username/email
- DOMPurify per output utente con HTML (motto, descrizioni)

### SQL Injection
- **mysql2 prepared statements** ovunque (`pool.execute()`)
- Mai string concatenation
- `multipleStatements: false` (default)
- Connection pool con timeout

### Security headers (su ogni risposta API)
- `Strict-Transport-Security: max-age=31536000; includeSubDomains; preload`
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Permissions-Policy: geolocation=(), microphone=(), camera=(), payment=()`
- `X-Permitted-Cross-Domain-Policies: none`
- `Cross-Origin-Opener-Policy: same-origin`
- `Cross-Origin-Resource-Policy: same-origin`

### CORS
- Whitelist esplicita di origini (niente `*`)
- `credentials: true` solo per le origini whitelistate

### Password breach check
- **HaveIBeenPwned API** con k-anonymity (SHA-1 prefix-5)
- Avviso al register se password compromessa
- Logging non-bloccante: se HIBP down, non blocca registrazione

### 2FA TOTP (staff)
- Riusiamo tabella `staff_mfa` esistente
- TOTP RFC 6238, finestra ±1 step (30s)
- Recovery codes (10 per user, hashed con Argon2)

### Audit log
- `cms_v3_audit_log` per eventi sensibili (login, password change, ban, SSO issue, …)
- Include user_id, action, ip, user_agent, JSON details, timestamp
- Mai modificabile via API (read-only via admin panel)

### SSO ticket per Nitro client
- Generato per-request, lifetime **60 secondi**
- 24 byte random hex, stored in `users.auth_ticket`
- EMU lo consuma una sola volta (consumed-or-expired)

### RCON/MUS bridge
- TCP loopback **127.0.0.1:3001** (rcon.allowed whitelist IP)
- **Anti-replay built-in**: ogni request include `nonce` (12-byte hex) + `ts` (unix seconds)
- **rcon.token** opzionale (env `RCON_TOKEN`) per defense-in-depth — costant-time compare lato EMU
- `executecommand` (full RCE) **non esposto** dal nostro `services/rcon.ts` helper map
- Tutti gli endpoint `/api/v2/staff/*` richiedono `requireRank(5)` + audit log HMAC

### Logging
- **pino** strutturato JSON
- Mai loggare password, token, dati sensibili (filtro esplicito su Authorization header)
- Request ID per tracing cross-service

## Difese pianificate (non ancora implementate)

- [ ] WAF rules (mod_security o Cloudflare): regex SQLi/XSS in pre-filter
- [ ] CAPTCHA su register e login dopo N fallimenti (hCaptcha o Turnstile)
- [ ] IP geolocation fingerprinting + alert su login da nuovo paese
- [ ] Email verification al register
- [ ] Push notification su login (via email) per device nuovi
- [ ] Anomaly detection (login a orari/IP insoliti)
- [ ] Honeypot field nei form per bot scrapers
- [ ] Subresource Integrity (SRI) su CDN assets
- [ ] Content Security Policy strict con nonce sulla risposta HTML del frontend (Vite preview)
- [ ] Server-Sent Events (SSE) o WebSocket TLS per real-time notifiche
- [ ] DB read replica con account read-only (least privilege)

## Aggiornamento dipendenze

```sh
yarn outdated          # vedi cosa va aggiornato
yarn upgrade-interactive --latest  # upgrade controllato
yarn audit             # check vulnerabilità note
```

Pinning di sicurezza: i secret in `.env` NON vengono mai committati (`.gitignore`).

## Reporting

Vulnerabilità → email security@habboproject (PGP key da pubblicare).
