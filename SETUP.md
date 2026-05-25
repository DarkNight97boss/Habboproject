# Habbo Retro — Setup locale (stack pulito e funzionante)

Stack canonico, testato end-to-end (login → stanza → avatar):

| Componente | Versione / Fonte |
|---|---|
| Emulatore | **Arcturus Morningstar 3.5.5** (release ufficiale git.krews.org/morningstar/Arcturus-Community) |
| Plugin WebSocket | **ms-websockets** (`NitroWebsockets-3.1.jar`) |
| Client | **nitro-react** (github.com/billsonnn/nitro-react) |
| Asset Nitro | **harrydev44/nitro-assets** (set completo: figure/furniture/effect/generic + gamedata) |
| DB | **BaseDB MS 3.5.5** (incluso nella release Morningstar) |
| Web server client | `php -S` (XAMPP) su :8090 |

## Prerequisiti
- JDK 11 (x64), Maven (solo se ricompili l'emu), Node ≥18 + Yarn (`corepack enable`), XAMPP (MariaDB + PHP), Git.

## 1) Database
```sql
CREATE DATABASE ms;
-- importa il dump base (da Morningstar_3.5.5.zip -> "base database/BaseDB MS 3.5.5.sql")
-- NB: MariaDB non supporta utf8mb4_0900_ai_ci -> sostituiscilo con utf8mb4_general_ci prima dell'import
```
Imposta la whitelist WebSocket (altrimenti il client non si connette):
```sql
UPDATE emulator_settings SET `value`='*' WHERE `key`='websockets.whitelist';
```

## 2) Emulatore (Arcturus-MS)
- Cartella `Arcturus-MS` = contenuto di `Morningstar_3.5.5.zip` (jar + config.ini + plugins).
- `config.ini`: `db.database=ms`, `db.username=root`, `db.password=` (XAMPP).
- Copia `NitroWebsockets-3.1.jar` in `Arcturus-MS/plugins/`.

## 3) Client + asset (un comando)
```
powershell -ExecutionPolicy Bypass -File build_clean_client.ps1
```
Clona nitro-react + harrydev44/nitro-assets, builda e assembla tutto in `client-dist`.

## 4) Avvio
```
powershell -ExecutionPolicy Bypass -File start_clean_stack.ps1
```
Avvia MariaDB + emulatore (3000/3001/2096) + client su :8090.

## 5) Login (SSO)
Senza CMS, genera l'SSO a mano:
```sql
UPDATE users SET auth_ticket='MIASSO', online='0' WHERE username='test';
```
Poi apri: `http://localhost:8090/index.html?sso=MIASSO&room=57`
ed entra in stanza dal Navigator (→ Mijn Wereld/My Rooms → Welcome).

## Note / lezioni apprese
- Gli asset DEVONO essere nel formato del renderer: usare il **converter ufficiale** (o un set come harrydev44), NON l'all-in-1 (formato Nitro V3, incompatibile con renderer 1.6.x).
- `room.nitro` e i bundle **generic** devono essere coerenti col renderer (i set 2022 davano `createRoomObjectAndInitalize('room') = null`).
- Il client si connette via `ws://127.0.0.1:2096` (ms-websockets); l'emulatore autentica via `auth_ticket` (SSO).
- L'utente di test `test` ha rank 7 (Administrator).

## Sicurezza
Hardening applicato (vedi commit `fix(security)`/`feat(security)`):
- **RCON**: token obbligatorio (`rcon.token` in `config.ini` = `RCON_PASS` in `API/config.php`), bind su loopback. Genera un secret forte e tienilo **fuori dal repo** (i config sono in `.gitignore`).
- **CMS** (`app/security.php`): header di sicurezza, protezione CSRF globale (Origin/Referer + token), SQLi/XSS chiusi, rate-limit login, cookie sessione HttpOnly/SameSite/Secure.
- **DB/secret**: `CMS/app/management/config.php`, `EMU/config.ini`, `API/config.php` NON sono tracciati → copia dai `.example` e imposta le credenziali. Le vecchie credenziali nello storico git vanno **ruotate** se diventano reali.
- Lasciare `debug.mode` **off** (auth_ticket single-use). `enc.enabled` può restare `false` con nitro.
- **Emulatore anti-DoS/scala**: rate-limit pacchetti aggregato con disconnessione (`GameMessageRateLimit`), cap sui `count` lato client in 10+ handler, guardia anti-crash nel decoder, timeout pre-login 20s (anti-slowloris), clamp `readString`, cap anti zip-bomb 16MB (`ZIP.inflate`), indice O(1) `userId→client`. `emulator_settings` consigliati: `debug.mode=0`, `runtime.threads`≈ (core) e `io.workergroup.threads`≈ (2×core), `db.pool` adeguato. Heap JVM in `start_clean_stack.ps1` (`-Xmx` in base alla RAM, G1GC).
- **Fix DB runtime (non nel repo, applicarli al DB):** creare la tabella `chat_bubbles` (type,name,permission,overridable,triggers_talking_furniture) e impostare in `emulator_settings` i path locali esistenti per `imager.location.badgeparts/output.badges/output.camera/output.thumbnail` (evita errori d'avvio non fatali).
- **IDOR/economia (fix codice):** consumo furni (seed monsterplant/pet-box) solo da proprietario/rights (`ToggleFloorItemEvent`); acquisto gilda scala crediti solo dopo le validazioni (`RequestGuildBuyEvent`).
- **Flood di connessioni**: NON limitati nell'emulatore (dietro Cloudflare l'emu vede solo IP CF). Gestiscili a livello **Cloudflare** (Rate Limiting Rules / "Under Attack Mode") o **firewall** (es. `iptables`/`nftables` connlimit sulle porte 3000/2096).

## Cloudflare (produzione)
Il codice è già predisposto (trust degli IP CF, HTTPS via `X-Forwarded-Proto`/`CF-Visitor`, HSTS, Secure cookie). Per attivarlo:
1. DNS del dominio su Cloudflare (proxy "arancione" attivo) → punta all'IP del server.
2. SSL/TLS in modalità **Full** (o Full Strict con certificato origin).
3. `config.php`: `$_CONFIG['hotel']['url']` (+ `cdnurl`, `swfurl`, `api.link`) col dominio **https://**.
4. **Turnstile** (captcha): crea un widget su Cloudflare e metti `sitekey`/`secretkey` in `config.php` (`$_CONFIG['cloudflare']`). In locale il captcha è saltato solo da `127.0.0.1`.
5. Apri solo le porte necessarie; WebSocket (2096): instradalo via CF (WS supportato sul proxy) o esponi `wss://` con TLS. Aggiorna `socket.url` del client di conseguenza.
6. I range IP CF sono in `app/security.php` (`hp_cloudflare_ranges()`); aggiornali se Cloudflare li cambia (cloudflare.com/ips).

## Blindatura porte emulatore (Cloudflare Spectrum / proxy L4)
La porta game raw (3000) e il WS (2096) NON possono andare sotto il proxy HTTP "arancione". Per proteggerle:

**Opzione A — Cloudflare Spectrum (consigliata):** crea un'app Spectrum (TCP) che instrada la porta pubblica → origine, con **PROXY protocol abilitato**. L'emulatore ora lo supporta:
- `emulator_settings`: `io.proxy.protocol.enabled=1` (e opzionale `io.proxy.protocol.trusted` = lista CIDR separati da `;`, default = range Cloudflare + loopback) e `networking.tcp.proxy=1`.
- Con Spectrum+PROXY: l'emu legge l'**IP reale** del client (ban/anti-clone corretti) e **rifiuta** ogni connessione non proveniente da IP Cloudflare → la porta è "cieca" agli attacchi diretti.
- Nitro WS (2096) dietro Cloudflare usa il proxy WS standard; ms-websockets inietta l'IP reale via `UserGetIPAddressEvent` quando `networking.tcp.proxy=1`.

**Opzione B — Firewall:** consenti 3000/2096 SOLO dagli IP Cloudflare (cloudflare.com/ips), droppa il resto (`iptables`/`nftables`). Aggiungi `connlimit`/`hashlimit` per i flood di connessione.

**"Porte random":** sconsigliato per servizi client-facing (il client deve conoscere la porta) → falso senso di sicurezza. L'equivalente robusto: Spectrum espone una porta pubblica gestita da CF mentre l'**origine usa una porta non standard, firewallata solo verso CF** (di fatto non scansionabile). Per il game flash interno puoi anche bindare `game.host=127.0.0.1` se usi solo nitro.
