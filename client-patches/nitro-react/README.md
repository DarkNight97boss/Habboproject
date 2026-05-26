# Staff MFA – patch client nitro-react (Parte 2)

Modal "stile Habbo" per la verifica in due fattori dello staff (Google
Authenticator), che sblocca i poteri staff dopo il login. Lavora con la Parte 1
lato emulatore (vedi `EMU/.../core/StaffMfa.java`, `Totp.java`, i pacchetti
`StaffMfa*` e la tabella `staff_mfa`).

## File di questo patch

Copia `src/components/staff-mfa/` nella tua copia di **nitro-react**:

```
src/components/staff-mfa/
  StaffMfaView.tsx                      # il modal
  messages/
    StaffMfaRequiredParser.ts           # in  9500: bool enrolled, str secret, str otpauthUri
    StaffMfaRequiredEvent.ts
    StaffMfaResultParser.ts             # in  9502: bool success, str message
    StaffMfaResultEvent.ts
    StaffMfaVerifyComposer.ts           # out 9501: str code
    StaffMfaStatusRequestComposer.ts    # out 9503: (nessun payload)
    StaffMfaMessageConfiguration.ts     # registra gli header sopra a runtime
    index.ts
```

I pacchetti custom vengono registrati a runtime con
`GetConnection().registerMessages(...)` dentro `StaffMfaView`, quindi **non**
serve modificare `@nitrots/nitro-renderer`.

## Unica modifica a un file esistente: `src/components/main/MainView.tsx`

1. Import (vicino agli altri import dei componenti):

```ts
import { StaffMfaView } from '../staff-mfa/StaffMfaView';
```

2. Monta il componente dentro il `<Base fit>` di ritorno (es. dopo
   `<FloorplanEditorView />`):

```tsx
<StaffMfaView />
```

## Header (devono combaciare con l'emulatore)

| Direzione        | Nome                         | Header |
|------------------|------------------------------|--------|
| server → client  | StaffMfaRequiredComposer     | 9500   |
| client → server  | StaffMfaVerifyEvent          | 9501   |
| server → client  | StaffMfaResultComposer       | 9502   |
| client → server  | StaffMfaStatusRequestEvent   | 9503   |

## Build & deploy del client

**Già fatto** in questa sessione (`yarn build` 20s, type-check pulito sull'intero
progetto). Bundle vite (`index-*.js`, `nitro-renderer-*.js`, `vendor-*.js`)
copiati in `Github/client-dist/assets/` insieme al nuovo `index.html`; i
`renderer-config.json`/`ui-config.json` locali NON sono stati toccati. Test HTTP
su `:8090` ritorna 200 su index e su tutti i bundle.

Per ri-buildare in futuro:

```powershell
# dalla cartella nitro-react
yarn install   # solo se mancano dipendenze
yarn build
# poi sostituisci client-dist/assets/* + client-dist/index.html con dist/
# (lascia stare renderer-config.json/ui-config.json se sono gia' personalizzati)
```

## Attivazione della feature (quando il client è pronto)

Di default è **spenta** per non bloccare lo staff prima del test. Per attivarla
sul DB `ms`:

```sql
UPDATE emulator_settings SET value='1' WHERE `key`='mfa.staff.enabled';
-- opzionale: oltre ai possessori del mod-tool, tratta come staff i rank >= N
UPDATE emulator_settings SET value='5' WHERE `key`='mfa.staff.min_rank';
```

Poi `:reload_config` (o riavvio emulatore). Al primo login lo staff vede il
popup con la chiave da inserire in Google Authenticator ("Enter a setup key");
ai login successivi solo il campo del codice a 6 cifre.

## Sicurezza

- TOTP RFC-6238 (SHA-1, 30s, 6 cifre), confronto a tempo costante, anti-replay
  (un codice/contatore usato una sola volta).
- La chiave segreta viaggia solo in fase di enrollment e **non** viene mai
  inviata a servizi esterni (niente QR di terze parti: inserimento manuale).
- Enforcement lato server: comandi con permesso e azioni mod-tool (ban/kick/
  mute/warn/alert/tradelock) sono bloccati finché la sessione non è verificata.
