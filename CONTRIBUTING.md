# Contributing ad Asteria Core / Habboproject

Benvenutoa nel progetto Asteria Core, il retro Habbo italiano. Questo documento spiega il **workflow di sviluppo PR -> dev -> prod**, come configurare l'ambiente, come fare il deploy e come gestire rollback ed emergenze.

> Lingua: **italiano** (codice, commit message e PR vanno bene anche in inglese tecnico, ma le discussioni e la documentazione restano in italiano).

---

## Indice

1. [Panoramica del flusso](#1-panoramica-del-flusso)
2. [Ambienti](#2-ambienti)
3. [Come aprire una Pull Request](#3-come-aprire-una-pull-request)
4. [Login al dev environment](#4-login-al-dev-environment)
5. [GitHub Secrets e Variables da configurare](#5-github-secrets-e-variables-da-configurare)
6. [Promotion a prod (branch protection)](#6-promotion-a-prod-branch-protection)
7. [Rollback](#7-rollback)
8. [Sviluppo locale](#8-sviluppo-locale)
9. [Troubleshooting](#9-troubleshooting)
10. [Convenzioni di codice](#10-convenzioni-di-codice)

---

## 1. Panoramica del flusso

Ogni modifica al codice passa attraverso questo ciclo:

```
   git checkout -b feat/xxx              PR open / push                merge to main
       |                                       |                            |
       v                                       v                            v
+-------------+   git push     +--------------------+    review      +-------------+
| Branch      |  ----------->  | Pull Request       |  ----------->  | main        |
| feat/xxx    |                | verso main         |   approve      | (protected) |
+-------------+                +---------+----------+                +------+------+
                                         |                                  |
                            pr-dev-deploy.yml (GHA)            prod-deploy.yml (GHA)
                                         |                                  |
                                         v                                  v
                        +-----------------------------+      +------------------------------+
                        | https://dev.asteriacore     |      | https://asteriacore.online   |
                        | .online                     |      | (produzione, utenti reali)   |
                        | DB ms_dev (reset ogni PR)   |      | DB ms (persistente)          |
                        | EMU :30001 / API :8093      |      | EMU :30000 / API :8092       |
                        +-----------------------------+      +------------------------------+
                                  ^                                          ^
                                  +---- Cloudflare Tunnel "asteria" ---------+
                                       (stesso tunnel, stesso VPS Hetzner)
```

**Regole d'oro:**

- Nessun commit diretto su `main`. Sempre tramite PR.
- Ogni PR aperta verso `main` triggera un **deploy automatico su `dev.asteriacore.online`** (singolo ambiente dev, condiviso e ri-utilizzato dalla PR piu recente).
- Solo il **merge** su `main` triggera il deploy su produzione.
- Il `main` e protetto: serve almeno **1 approval** e tutti i status check verdi.
- Hotfix critici: PR diretta a `main` con label `hotfix`, comunque richiede review (anche post-merge in caso di rollback).

---

## 2. Ambienti

| Ambiente | URL pubblico | DB | EMU port | CMS-API port | Bucket R2 | Branch / trigger |
|---|---|---|---|---|---|---|
| **Prod** | `https://asteriacore.online` (+ `api.`, `hotel.`) | `ms` | 30000 | 8092 | `asteria-gamedata` | merge su `main` |
| **Dev** | `https://dev.asteriacore.online` (+ `dev-api.`, `dev-hotel.`) | `ms_dev` (DROP+CREATE ad ogni nuova PR) | 30001 | 8093 | `asteria-gamedata-dev` | apertura/push PR -> `main` |
| **Locale** | `http://localhost:8080` (CMS) + `localhost:3000` (Vite) + EMU `localhost:30000` | `ms` (locale) | 30000 | varia | nessuno (asset locali) | sviluppo libero |

Entrambi gli ambienti remoti girano sullo **stesso VPS Hetzner CPX21** (REDACTED-VPS-IP), con **systemd unit paralleli** e DB MariaDB separati. Niente Docker: due processi distinti, due porte distinte, due directory distinte (`/opt/habboproject` per prod, `/opt/habboproject-dev` per dev).

---

## 3. Come aprire una Pull Request

### 3.1 Branch naming

Usa un prefisso che descrive il tipo di lavoro:

| Prefisso | Quando usarlo | Esempio |
|---|---|---|
| `feat/` | Nuova feature utente o staff | `feat/battlepass-season2` |
| `fix/` | Bug fix non critico | `fix/trade-window-flicker` |
| `hotfix/` | Bug critico in prod (fast-track) | `hotfix/login-500-error` |
| `chore/` | Refactor, dipendenze, build | `chore/bump-react-19.1` |
| `db/` | Migration schema DB | `db/add-staff-mfa-table` |
| `nitro/` | Modifiche al client Nitro V3 / asset | `nitro/new-room-shader` |
| `docs/` | Solo documentazione | `docs/update-deploy-guide` |
| `security/` | Audit / fix di sicurezza | `security/wave18-acl-fix` |

### 3.2 Workflow

```powershell
# da Windows / PowerShell, nella root C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject
git checkout main
git pull origin main
git checkout -b feat/nome-breve-descrittivo

# ... modifica i file ...

git add path/specifico/al/file
git commit -m "feat: descrizione concisa di cosa fa la modifica"
git push -u origin feat/nome-breve-descrittivo
```

Apri poi la PR via GitHub UI oppure:

```powershell
gh pr create --base main --head feat/nome-breve-descrittivo --title "feat: titolo" --body "## Cosa cambia`n...`n## Test plan`n- [ ] ..."
```

### 3.3 Labels speciali

Le seguenti label modificano il comportamento dei workflow CI:

| Label | Effetto |
|---|---|
| `wip` | **Skip dev deploy.** Utile mentre stai ancora sviluppando e non vuoi sprecare cicli di build. Rimuovi la label quando sei pronto per il preview. |
| `deploy-prod` | **Fast-track verso prod** dopo merge. Nessun cambiamento sul workflow di per se (il deploy prod parte comunque al merge), ma segnala ai reviewer che la PR vuole arrivare in produzione il prima possibile (es. hotfix). |
| `hotfix` | Marca la PR come fix urgente in prod. Combinata con `deploy-prod` per emergenze. |
| `db-migration` | La PR contiene file in `EMU/sqlupdates/` o `CMS-V3/apps/api/src/db/migrations/`. Il deploy applica le migrations nuove (dev: drop+ricrea; prod: incrementale). |
| `nitro-bundle` | La PR tocca `CMS/react/**` o `Nitro-V3/**`. Triggera lo step di sync su R2 dev (e su R2 prod al merge). |
| `skip-ci` (nel commit message, non come label) | Salta completamente i workflow. Da usare solo per modifiche puramente cosmetiche al README. |

### 3.4 Cosa succede dopo l'apertura della PR

1. Il workflow `.github/workflows/pr-dev-deploy.yml` parte automaticamente.
2. Controlla che l'autore sia nella `DEPLOY_ALLOWLIST` (vedi sezione 5). Altrimenti il workflow si ferma con un messaggio chiaro nella PR.
3. Rileva i path modificati (`dorny/paths-filter`): se la PR tocca solo il frontend, skip della build EMU (risparmio ~70s).
4. Build su `ubuntu-latest`: EMU jar via maven (cache), CMS-V3 web via Vite (cache yarn), API tsc.
5. Rsync degli artifact verso il VPS in `/tmp/asteria-dev-<sha>/`.
6. Lo script `apply-dev.sh` (gia presente sul VPS) atomicamente:
   - Drop+ricrea `ms_dev`, importa `BaseDB MS 3.5.5.sql` + tutti gli `sqlupdates/*.sql` + tutti i `CMS-V3/.../migrations/*.sql`.
   - Switcha i symlink di `/opt/habboproject-dev/current` al nuovo build.
   - `systemctl restart habbo-emu-dev cms-api-dev`.
7. Health check: curl di `https://dev.asteriacore.online/health` + check EMU socket.
8. Bot commenta sulla PR con:
   - URL preview: `https://dev.asteriacore.online`
   - SHA deployato
   - Tempo di build totale
   - Eventuali warning (es. nuove migrations applicate, asset Nitro re-sync'ati)

Se apri **una seconda PR** mentre la prima e ancora aperta: la prima viene "sfrattata" dal dev (concurrency group `dev`, `cancel-in-progress: true`). Il dev environment punta sempre all'**ultima PR push'ata**. Non e uno staging multi-tenant: e un single-slot preview.

---

## 4. Login al dev environment

Il DB `ms_dev` viene **droppato e ricreato ad ogni nuova PR**, quindi gli utenti di test vanno ricreati ogni volta.

### 4.1 Creare un utente staff di test sul dev

Apri `https://dev.asteriacore.online`, vai su "Registrati" e crea un account normale. Poi via SSH al VPS:

```bash
ssh asteria@REDACTED-VPS-IP
mysql -u arcturus_dev -p ms_dev
```

```sql
-- promuovi l'utente che hai appena creato a staff con rank max
UPDATE users SET rank = 7 WHERE username = 'iltuonome';

-- abilita MFA bypass per il dev (opzionale, solo se la wave 17 e attiva)
UPDATE users SET staff_mfa_enabled = 0 WHERE rank >= 5;

-- crediti / diamanti per test
UPDATE users SET credits = 999999, points = 999999, vip_points = 999999 WHERE username = 'iltuonome';
```

### 4.2 Login giocatore normale

Stessa procedura ma senza il `UPDATE rank`. Per testare un flow guest/nuovo player, usa la registrazione normale senza modificare nulla.

### 4.3 Reset esplicito del dev (utile se i dati di test sono corrotti)

```bash
ssh asteria@REDACTED-VPS-IP
sudo /opt/habboproject-dev/bin/reset-dev-db.sh
sudo systemctl restart habbo-emu-dev cms-api-dev
```

Lo script e idempotente e ricrea `ms_dev` partendo dal SQL della PR attualmente deployata.

### 4.4 Test del client Nitro V3 sul dev

Se la PR ha la label `nitro-bundle`, il bundle Nitro nuovo e gia stato pushato su `asteria-gamedata-dev` (R2). Il client servito da `https://dev.asteriacore.online/client` lo carica automaticamente.

Se invece la PR **non** modifica il client, il dev usa lo stesso bundle prod (`asteria-gamedata`). Questo permette di testare cambi server-side senza dover ribuildare il client.

---

## 5. GitHub Secrets e Variables da configurare

Vai in **Settings -> Secrets and variables -> Actions** del repo `DarkNight97boss/Habboproject`. Configura **una sola volta** quanto segue.

### 5.1 Repository secrets (tab "Secrets")

| Nome | Tipo | Descrizione | Come generarlo |
|---|---|---|---|
| `SSH_PRIVATE_KEY` | Ed25519 private key | Deploy key per dev. Public key in `/home/asteria/.ssh/authorized_keys` sul VPS. | `ssh-keygen -t ed25519 -C asteria-ci-dev -f ./ci-dev-key` (no passphrase). Carica il `.pub` sul VPS, incolla il `.priv` qui. |
| `SSH_KNOWN_HOSTS` | testo (1 riga) | Fingerprint del VPS per evitare MITM. | `ssh-keyscan -t ed25519,rsa REDACTED-VPS-IP` -> copia output |
| `CF_API_TOKEN` | Cloudflare API token | Solo se il workflow gestisce DNS/tunnel dinamici. Setup statico = puoi anche ometterlo. | Cloudflare dashboard -> My Profile -> API Tokens -> Create Token. Scope: Zone:Read+Edit su `asteriacore.online`, Tunnel:Edit su account `d205b08a93a3a81098cdaabdbcaa1548`. |
| `R2_DEV_ACCESS_KEY_ID` | stringa | Access key R2 scoped al bucket `asteria-gamedata-dev`. | Cloudflare dashboard -> R2 -> Manage API tokens -> Create. Permission: Object Read+Write su bucket dev. |
| `R2_DEV_SECRET_ACCESS_KEY` | stringa | Secret della stessa coppia sopra. | Mostrato una sola volta in fase di creazione del token R2. |
| `DB_DEV_PASSWORD` | stringa | Password user `arcturus_dev` su MariaDB. Opzionale (di solito sta solo sul VPS in `/etc/asteria/cms-api-dev.env`). | Vedi password in `/root/.asteria-secrets/db.env` sul VPS. |
| `DISCORD_WEBHOOK_OPS` | URL | Webhook canale `#ops-deploy` per notifiche success/failure prod. | Discord -> Edit Channel -> Integrations -> Webhooks -> New. |

### 5.2 Environment secrets (Environment: `production`)

Crea l'environment **"production"** in **Settings -> Environments -> New environment**. Aggiungi:

- **Required reviewers:** `DarkNight97boss` (e opzionalmente `simoleo89`). Questo gateaa manualmente il deploy prod anche se il merge avviene automaticamente.
- **Wait timer:** 0 minuti (oppure 5 minuti se vuoi una finestra di "annulla deploy" in caso di errore).

Poi aggiungi questi secrets **scoped solo a questo environment**:

| Nome | Descrizione |
|---|---|
| `SSH_PRIVATE_KEY_PROD` | Chiave SSH **separata** da quella dev, dedicata al deploy prod. Public key in `authorized_keys` del VPS su linea separata. |
| `R2_PROD_ACCESS_KEY_ID` | Access key R2 scoped al bucket `asteria-gamedata` (prod). |
| `R2_PROD_SECRET_ACCESS_KEY` | Secret corrispondente. |

### 5.3 Repository variables (tab "Variables")

Le variables sono **non-secret** e si possono modificare senza compromettere le chiavi. Configura:

| Nome | Valore esempio | Descrizione |
|---|---|---|
| `VPS_HOST` | `REDACTED-VPS-IP` | IP del VPS Hetzner |
| `VPS_USER` | `asteria` | User SSH sul VPS |
| `CF_ACCOUNT_ID` | `d205b08a93a3a81098cdaabdbcaa1548` | Account Cloudflare |
| `CF_ZONE_ID` | `184fbdcde81f5bb7f9edd4a9eb30fd19` | Zone ID di `asteriacore.online` |
| `CF_TUNNEL_ID` | `4e4aa9aa-46ea-4399-9d71-c2ded67629d1` | Tunnel "asteria" |
| `DEPLOY_ALLOWLIST` | `["simoleo89","DarkNight97boss"]` | Lista JSON degli author le cui PR triggerano il dev deploy. Aggiungi collaboratori qui senza toccare i secrets. |
| `PROD_DEPLOY_ALLOWLIST` | `["simoleo89","DarkNight97boss"]` | Lista ristretta per i merge su `main`. Usata in `prod-deploy.yml` per verificare `github.actor`. |

### 5.4 Secrets che NON stanno su GitHub

Questi vivono solo sul VPS:

- `/root/.asteria-secrets/db.env` - password MariaDB user `arcturus` e `arcturus_dev`
- `/root/.asteria-secrets/gh.env` - token GitHub fine-grained (`GH_TOKEN_VPS`) usato dallo script `teardown-dev.sh` per fare `gh pr list --state open`
- `/etc/cloudflared/credentials.json` - credenziali del tunnel
- `/etc/asteria/cms-api-prod.env` e `/etc/asteria/cms-api-dev.env` - config runtime delle API

**Non committarli mai nel repo.** Sono `chmod 600` e leggibili solo da `root` / `asteria`.

---

## 6. Promotion a prod (branch protection)

Configura **Settings -> Branches -> Add branch protection rule** per `main`:

### 6.1 Regole obbligatorie

- [x] **Require a pull request before merging**
  - [x] Require approvals: **1**
  - [x] Dismiss stale pull request approvals when new commits are pushed
  - [x] Require review from Code Owners (se hai un `CODEOWNERS` file)
- [x] **Require status checks to pass before merging**
  - [x] Require branches to be up to date before merging
  - [x] Status check richiesti:
    - `pr-dev-deploy / build-emu` (se la PR tocca EMU)
    - `pr-dev-deploy / build-cms` (sempre)
    - `pr-dev-deploy / deploy-dev` (deploy a dev riuscito)
    - `pr-dev-deploy / health-check` (dev risponde 200)
- [x] **Require conversation resolution before merging**
- [x] **Require signed commits** (opzionale ma raccomandato)
- [x] **Require linear history** (no merge commits, solo squash o rebase)
- [x] **Do not allow bypassing the above settings** (anche admin)
- [x] **Restrict who can push to matching branches:** solo `DarkNight97boss` e `simoleo89`

### 6.2 Regole raccomandate

- [x] **Require deployments to succeed before merging:** environment `dev` (cosi non puoi mergiare se il dev non sta su)
- [ ] **Lock branch** (no, lo lascia editabile)
- [x] **Block force pushes**
- [x] **Block deletions**

### 6.3 Cosa succede al merge

1. La PR viene mergiata su `main` (squash preferito per pulizia history).
2. Il workflow `.github/workflows/prod-deploy.yml` parte.
3. Aspetta l'approval del reviewer (environment `production`).
4. Build identico al dev ma con `NODE_ENV=production`.
5. Rsync degli artifact verso `/opt/habboproject` (NON `-dev`).
6. Lo script `apply-prod.sh`:
   - Applica solo migrations **nuove** (incrementale, tracciato da tabella `schema_migrations`). NON droppa il DB.
   - In-game broadcast `"Riavvio server tra 60 secondi per aggiornamento, salva il tuo lavoro!"` (via comando staff `/broadcast`).
   - Sleep 60 secondi.
   - `systemctl reload-or-restart cms-api` (Hono boot ~150ms, near-zero downtime).
   - `systemctl restart habbo-emu` (5-10s downtime, accettabile).
7. Health check su `https://asteriacore.online` + `/api/health`.
8. Notifica su Discord `#ops-deploy` con SHA, autore merge, lista commit.

---

## 7. Rollback

Tre scenari, in ordine di gravita crescente.

### 7.1 Rollback "soft" - revert della PR

Se il problema e identificato entro pochi minuti e non c'e corruzione dati:

```powershell
# trova lo SHA del merge commit incriminato
gh pr list --state merged --limit 5
gh pr view <PR_NUMBER>

# revert tramite GitHub UI: vai sulla PR -> "Revert" button -> apri PR di revert -> merge
# oppure da CLI:
git checkout main
git pull
git revert -m 1 <merge_sha>
git push origin main
```

Il revert e una nuova PR che a sua volta triggera il flusso completo (dev preview -> approval -> deploy prod). Tempo totale: ~5 minuti se approvato subito.

### 7.2 Rollback "hard" - redeploy versione precedente direttamente sul VPS

Se serve essere veloci e non si vuole aspettare un altro ciclo di build:

```bash
ssh asteria@REDACTED-VPS-IP
cd /opt/habboproject

# lista le release passate (mantenute in /opt/habboproject/releases/<sha>/)
ls -lht releases/ | head -10

# switch atomico al symlink della release precedente
sudo /opt/habboproject/bin/rollback.sh <sha_precedente>

# lo script fa:
#   - ln -sfn releases/<sha>/web current/web
#   - ln -sfn releases/<sha>/api current/api
#   - cp releases/<sha>/emu.jar /opt/habboproject/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar
#   - systemctl restart cms-api habbo-emu
```

**Importante:** dopo un hard rollback, apri **comunque** una PR di revert su GitHub. Altrimenti il prossimo deploy automatico re-introdurra il bug (perche il VPS sara fuori sync con `main`).

### 7.3 Rollback DB (caso peggiore)

Se la migration che ha causato il problema ha alterato lo schema:

```bash
ssh asteria@REDACTED-VPS-IP

# backup automatici (cron) in /var/backups/mariadb/ms-<timestamp>.sql.gz
ls -lht /var/backups/mariadb/ | head -10

# stop API + EMU prima di ripristinare
sudo systemctl stop cms-api habbo-emu

# ripristino full
zcat /var/backups/mariadb/ms-<timestamp_precedente_alla_migration>.sql.gz | sudo mysql ms

# rimuovi anche la riga in schema_migrations cosi non si crede applicata
mysql -u root ms -e "DELETE FROM schema_migrations WHERE filename = 'NNN_nome_migration.sql';"

# riavvia
sudo systemctl start habbo-emu cms-api
```

Il cron di backup gira ogni 6 ore (`/etc/cron.d/asteria-db-backup`), quindi nel peggiore dei casi perdi 6 ore di dati. Per perdere meno, considera di aumentare la frequenza dopo aver crescuto la playerbase.

---

## 8. Sviluppo locale

Vedi anche `MEMORY.md` (habboproject-local-setup) per i dettagli su porte e gotchas. Quick start:

### 8.1 CMS-V3 frontend (React 19 + Vite 8)

```powershell
cd C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\CMS-V3\apps\web
yarn install
yarn dev
# http://localhost:3000
```

### 8.2 CMS-V3 API (Hono Node)

```powershell
cd C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\CMS-V3\apps\api
yarn install
yarn dev
# http://localhost:8092 (default, configurabile via .env)
```

Crea un `.env` locale (NON committarlo, gia in `.gitignore`):

```env
DATABASE_URL=mysql://arcturus:LA_TUA_PASSWORD@localhost:3306/ms
JWT_SECRET=qualche_stringa_lunga_random
NODE_ENV=development
PORT=8092
```

### 8.3 EMU Java

```powershell
cd C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\EMU
# usa JDK 11 oppure JDK 17 (entrambi vanno, vedi MEMORY local-setup)
mvn package -DskipTests
java -jar target/Habbo-3.5.5-jar-with-dependencies.jar
```

### 8.4 Nitro V3 client

```powershell
cd C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\CMS\react
yarn install
yarn build
# l'output dist/ va servito staticamente, oppure caricato su R2 dev manualmente per testare via tunnel
```

> **Nota:** lo script `yarn build` del Nitro e bloccato dal classifier. Devi essere **tu** (utente) a lanciarlo manualmente. Vedi `MEMORY.md` (habboproject-nitro-blocked).

### 8.5 Test rapido fullstack senza deploy

Quando vuoi testare un cambio integrato (CMS + EMU + client) prima ancora di aprire la PR:

```powershell
# 1. Avvia MariaDB locale (XAMPP / WAMP / Docker, vedi MEMORY local-setup)
# 2. Importa BaseDB MS 3.5.5.sql + sqlupdates/*.sql nel DB locale 'ms'
# 3. yarn dev in CMS-V3/apps/api  (terminal 1)
# 4. yarn dev in CMS-V3/apps/web  (terminal 2)
# 5. java -jar target/Habbo-3.5.5-jar-with-dependencies.jar (terminal 3)
# 6. Apri http://localhost:3000 e gioca
```

---

## 9. Troubleshooting

### 9.1 Il dev deploy fallisce - cosa controllo per primo?

1. **Apri il run del workflow** su GitHub Actions (link nel commento della PR). Cerca lo step rosso.
2. Casi tipici:
   - **`Author not in DEPLOY_ALLOWLIST`** -> aggiungi l'username alla variable `DEPLOY_ALLOWLIST` (vedi 5.3).
   - **`yarn install` fallisce con `ERESOLVE`** -> conflitto deps. Controlla `package.json` e rigenera `yarn.lock`.
   - **`mvn package` fallisce** -> errore di compilazione Java. Logs hanno il file:linea.
   - **`rsync: connection refused`** -> il VPS non e raggiungibile o l'SSH key e sbagliata. SSH manuale per verificare:
     ```powershell
     ssh -i .\ci-dev-key asteria@REDACTED-VPS-IP 'echo ok'
     ```
   - **`health check failed: curl: (52) empty reply`** -> il servizio e partito male sul VPS. SSH e guarda:
     ```bash
     ssh asteria@REDACTED-VPS-IP
     sudo journalctl -u cms-api-dev -n 100 --no-pager
     sudo journalctl -u habbo-emu-dev -n 100 --no-pager
     ```

### 9.2 Dev sito ritorna 502/504

Il backend e morto. Controlla i servizi:

```bash
ssh asteria@REDACTED-VPS-IP
sudo systemctl status cms-api-dev habbo-emu-dev
sudo journalctl -u cms-api-dev -n 200 --no-pager
```

Restart manuale:

```bash
sudo systemctl restart cms-api-dev habbo-emu-dev
```

### 9.3 Migration fallisce sul dev

Spesso e perche la migration assume uno stato del DB che non esiste piu (es. tabella gia presente da una `sqlupdate` precedente).

```bash
ssh asteria@REDACTED-VPS-IP
sudo /opt/habboproject-dev/bin/apply-sql.sh dev --verbose 2>&1 | tail -50
```

Forza un drop completo e reimport:

```bash
sudo /opt/habboproject-dev/bin/reset-dev-db.sh
```

### 9.4 Cloudflare Tunnel non instrada `dev.asteriacore.online`

Verifica:

```bash
ssh asteria@REDACTED-VPS-IP
sudo cat /etc/cloudflared/config.yml | head -30
sudo systemctl status cloudflared
```

Se mancano le tre route `dev.*`, aggiungi nel file e riavvia:

```bash
sudo systemctl restart cloudflared
```

Oppure verifica dalla dashboard Zero Trust che i Public Hostnames esistano:
- `dev.asteriacore.online -> http://localhost:80`
- `dev-api.asteriacore.online -> http://localhost:80`
- `dev-hotel.asteriacore.online -> http://localhost:80`

### 9.5 Prod e giu - dove guardo?

```bash
ssh asteria@REDACTED-VPS-IP
sudo systemctl status habbo-emu cms-api cloudflared nginx mariadb
sudo journalctl -u habbo-emu -n 100 --no-pager
sudo journalctl -u cms-api -n 100 --no-pager
```

Se tutto sembra a posto ma il sito non risponde: **e Cloudflare** (incident upstream). Vai su `https://www.cloudflarestatus.com/`.

### 9.6 Asset Nitro vecchi serviti dal client

Il bundle e cachato su Cloudflare CDN. Force purge:

```bash
# da locale o da CI:
curl -X POST "https://api.cloudflare.com/client/v4/zones/184fbdcde81f5bb7f9edd4a9eb30fd19/purge_cache" \
  -H "Authorization: Bearer $CF_API_TOKEN" \
  -H "Content-Type: application/json" \
  --data '{"purge_everything":true}'
```

Oppure aspetta la TTL (default 4h su asset statici).

### 9.7 Il bot di GitHub non commenta sulla PR

Il workflow non ha il permesso `pull-requests: write`. Verifica nel YAML:

```yaml
permissions:
  contents: read
  pull-requests: write
```

E in **Settings -> Actions -> General -> Workflow permissions** -> seleziona "Read and write permissions".

---

## 10. Convenzioni di codice

### 10.1 Commit messages (Conventional Commits)

```
feat: aggiungi battlepass season 2
fix: corregge crash su trade window con item null
chore: bump react a 19.1.0
db: aggiunge tabella staff_mfa
nitro: nuovo shader stanza VIP
docs: aggiorna CONTRIBUTING.md
security: fix SQL injection in /api/admin/users (wave 18)
hotfix: ripristina endpoint /login dopo refactor JWT
```

Per commit che includono breaking changes:

```
feat!: cambia formato JWT token (logout forzato per tutti gli utenti)

BREAKING CHANGE: il vecchio formato JWT non e piu valido. Tutti gli utenti
devono fare login di nuovo. Aggiornare anche il client Nitro V3 a >= 3.2.0.
```

### 10.2 PR title

Stessa convention del commit. Il merge usa squash, quindi il PR title diventa il commit message su `main`.

### 10.3 Code style

- **TypeScript:** ESLint + Prettier (config in `CMS-V3/.eslintrc.json`). `yarn lint` prima di pushare.
- **Java:** Google Java Style. IntelliJ ha l'importer pronto.
- **SQL migrations:** una per file, naming `NNN_nome_descrittivo.sql` (es. `042_add_battlepass_progress.sql`). MAI modificare una migration gia applicata in prod - crea sempre una nuova.

### 10.4 Sicurezza

- **MAI committare:** `.env`, `.env.local`, `*.key`, `*.pem`, file con `password`/`secret`/`token` nel nome.
- Il pre-commit hook (se installato via `husky`) blocca questi pattern automaticamente.
- Prima di pushare, scan rapido:
  ```powershell
  git diff --cached | Select-String -Pattern "password|secret|api_key|BEGIN PRIVATE KEY" -CaseSensitive:$false
  ```
- Se hai accidentalmente committato un secret: **non basta rimuoverlo nel commit successivo**. Va rotato immediatamente (genera nuova chiave, aggiorna su VPS + GitHub Secrets) e idealmente rimosso dalla history con `git filter-repo`.

### 10.5 Review checklist (per chi approva)

- [ ] Il dev preview funziona? (apri `https://dev.asteriacore.online`, login, click GIOCA, gameroom)
- [ ] Test plan della PR completato?
- [ ] Nessun secret committato?
- [ ] Migrations idempotenti (se presenti)?
- [ ] Eventuali breaking changes documentati?
- [ ] Se tocca staff/admin: ACL verificato?
- [ ] Se tocca pagamenti / crediti: testato che non si possa duplicare?

---

## Domande?

- Aprire una issue su GitHub con label `question`.
- Discord `#dev` per chiacchiere informali.
- `@DarkNight97boss` o `@simoleo89` per cose urgenti.

Buon coding!
