# Security Policy — Asteria Core

## Versione supportata
Solo `main` branch riceve fix di sicurezza.

## Segnalare una vulnerabilità

**Non aprire issue pubbliche.** Usa Private Vulnerability Reporting:
1. Vai su https://github.com/DarkNight97boss/Habboproject/security/advisories/new
2. Descrivi impatto, riproducibilità, PoC se possibile
3. Risposta entro 72h

**Bug-bounty informale**: ringraziamento pubblico + badge "Sicurezza" in-game per RCE/SQLi/SSRF/Auth-bypass.

## Aree sensibili
- routes/auth.ts, routes/shop.ts, services/stripe.ts
- EMU/core/AuditLog.java (HMAC chain)
- EMU/users/HabboManager.java (loadHabbo SSO + IP binding)
- .github/workflows/ (deploy secrets)

## Hardening attivo
- CF Tunnel (IP nascosto)
- UFW deny incoming default (CF whitelist)
- nginx bind 127.0.0.1
- bcrypt cost 12 + HIBP register check
- JWT HS256 1h + refresh rotation
- SSO ticket 300s + IP binding + atomic single-use
- Audit log HMAC-SHA256 chain
- MFA staff TOTP + recovery codes
- nginx Cloudflare real_ip module
- Branch protection main (no force-push, linear history)
- Secret scanning + push protection
