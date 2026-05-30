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
- **Argon2id** per password hashing (memory 64MB, time 3, parallelism 4) — OWASP 2024
- **JWT access token** HS256, vita **15 min**, claims: sub, username, rank, jti, iss, aud
- **Refresh token** HS256 con secret SEPARATO da JWT, vita **14 giorni**, salvato in DB
- **Rotation** automatica refresh: ad ogni uso emette nuovo + invalida vecchio
- **Reuse detection**: se un refresh token già "used_at" viene ripresentato → revoca dell'**intera family** (signal di furto)
- **Migrazione trasparente**: la prima login con hash legacy (bcrypt/md5) genera nuovo Argon2 e salva

### Cookies
- `cms_v3_access` e `cms_v3_refresh`: **HttpOnly**, **Secure** (in prod), **SameSite=Lax**
- Path=/, Max-Age esplicito, no Domain leak

### CSRF
- Double-submit cookie pattern: cookie `cms_v3_csrf` + header `X-CSRF-Token` ad ogni stato-mutating
- SameSite=Lax sui cookie come secondo livello

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
