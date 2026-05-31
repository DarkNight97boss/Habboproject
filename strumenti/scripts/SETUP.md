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
