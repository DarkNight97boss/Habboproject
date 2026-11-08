# Contributing ad Asteria Core / Habboproject

Benvenutoa nel progetto Asteria Core, il retro Habbo italiano. Questo documento spiega il **workflow di sviluppo PR -> prod**, come configurare l'ambiente, come fare il deploy e come gestire rollback ed emergenze.

> Lingua: **italiano** (codice, commit message e PR vanno bene anche in inglese tecnico, ma le discussioni e la documentazione restano in italiano).

---

## Indice

1. [Panoramica del flusso](#1-panoramica-del-flusso)
2. [Ambienti](#2-ambienti)
3. [Come aprire una Pull Request](#3-come-aprire-una-pull-request)
4. [GitHub Secrets e Variables da configurare](#4-github-secrets-e-variables-da-configurare)
5. [Promotion a prod (branch protection)](#5-promotion-a-prod-branch-protection)
6. [Rollback](#6-rollback)
7. [Sviluppo locale](#7-sviluppo-locale)
8. [Troubleshooting](#8-troubleshooting)
9. [Convenzioni di codice](#9-convenzioni-di-codice)

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
                                   ci.yml (GHA)                     prod-deploy.yml (GHA)
                          typecheck / lint / test / build                     |
                          + codeql / semgrep / gitleaks                       v
                                                              +------------------------------+
                                                              | https://asteriacore.online   |
                                                              | (produzione, utenti reali)   |
                                                              | DB ms (persistente)          |
                                                              | EMU :30000 / API :8092       |
                                                              +------------------------------+
                                                                             ^
                                                              Cloudflare Tunnel "asteria"
                                                                (VPS Hetzner)
```

**Regole d'oro:**

- Nessun commit diretto su `main`. Sempre tramite PR.
- Ogni PR aperta verso `main` esegue i check CI (`ci.yml`: typecheck, lint, test, build; piu codeql, semgrep, gitleaks). Non esiste un ambiente di preview.
- Solo il **merge** su `main` triggera il deploy su produzione.
- Il `main` e protetto: serve almeno **1 approval** e tutti i status check verdi.
- Hotfix critici: PR diretta a `main` con label `hotfix`, comunque richiede review (anche post-merge in caso di rollback).

---

## 2. Ambienti

| Ambiente | URL pubblico | DB | EMU port | CMS-API port | Bucket R2 | Branch / trigger |
|---|---|---|---|---|---|---|
| **Prod** | `https://asteriacore.online` (+ `api.`, `hotel.`) | `ms` | 30000 | 8092 | `asteria-gamedata` | merge su `main` |
| **Locale** | `http://localhost:8080` (CMS) + `localhost:3000` (Vite) + EMU `localhost:30000` | `ms` (locale) | 30000 | varia | nessuno (asset locali) | sviluppo libero |

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
| `wip` | Segnala che la PR e ancora in lavorazione e non e pronta per la review. Rimuovi la label quando sei pronto. |
| `deploy-prod` | **Fast-track verso prod** dopo merge. Nessun cambiamento sul workflow di per se (il deploy prod parte comunque al merge), ma segnala ai reviewer che la PR vuole arrivare in produzione il prima possibile (es. hotfix). |
| `hotfix` | Marca la PR come fix urgente in prod. Combinata con `deploy-prod` per emergenze. |
| `db-migration` | La PR contiene file in `EMU/sqlupdates/` o `CMS-V3/apps/api/src/db/migrations/`. Il deploy prod applica le migrations nuove (incrementale). |
| `nitro-bundle` | La PR tocca `CMS/react/**` o `Nitro-V3/**`. Al merge triggera lo step di sync su R2 prod. |
| `skip-ci` (nel commit message, non come label) | Salta completamente i workflow. Da usare solo per modifiche puramente cosmetiche al README. |

### 3.4 Cosa succede dopo l'apertura della PR

1. Il workflow `.github/workflows/ci.yml` parte automaticamente ed esegue typecheck, lint, test e build.
2. In parallelo girano i check di sicurezza: CodeQL, Semgrep e Gitleaks.
3. Tutti i check devono essere verdi prima del merge (vedi sezione 5.1).
4. Non c'e nessun deploy preview: il deploy parte solo al merge su `main` (`prod-deploy.yml`, vedi sezione 5.3).

---

## 4. GitHub Secrets e Variables da configurare

Vai in **Settings -> Secrets and variables -> Actions** del repo `DarkNight97boss/Habboproject`. Configura **una sola volta** quanto segue.

### 4.1 Repository secrets (tab "Secrets")

| Nome | Tipo | Descrizione | Come generarlo |
|---|---|---|---|
| `SSH_KNOWN_HOSTS` | testo (1 riga) | Fingerprint del VPS per evitare MITM. | `ssh-keyscan -t ed25519,rsa REDACTED-VPS-IP` -> copia output |
| `CF_API_TOKEN` | Cloudflare API token | Solo se il workflow gestisce DNS/tunnel dinamici. Setup statico = puoi anche ometterlo. | Cloudflare dashboard -> My Profile -> API Tokens -> Create Token. Scope: Zone:Read+Edit su `asteriacore.online`, Tunnel:Edit su account `d205b08a93a3a81098cdaabdbcaa1548`. |
| `DISCORD_WEBHOOK_OPS` | URL | Webhook canale `#ops-deploy` per notifiche success/failure prod. | Discord -> Edit Channel -> Integrations -> Webhooks -> New. |

### 4.2 Environment secrets (Environment: `production`)

Crea l'environment **"production"** in **Settings -> Environments -> New environment**. Aggiungi:

- **Required reviewers:** `DarkNight97boss` (e opzionalmente `simoleo89`). Questo gateaa manualmente il deploy prod anche se il merge avviene automaticamente.
- **Wait timer:** 0 minuti (oppure 5 minuti se vuoi una finestra di "annulla deploy" in caso di errore).

Poi aggiungi questi secrets **scoped solo a questo environment**:

| Nome | Descrizione |
|---|---|
| `SSH_PRIVATE_KEY_PROD` | Chiave SSH dedicata al deploy prod. Public key in `authorized_keys` del VPS. |
| `R2_PROD_ACCESS_KEY_ID` | Access key R2 scoped al bucket `asteria-gamedata` (prod). |
| `R2_PROD_SECRET_ACCESS_KEY` | Secret corrispondente. |

### 4.3 Repository variables (tab "Variables")

Le variables sono **non-secret** e si possono modificare senza compromettere le chiavi. Configura:

| Nome | Valore esempio | Descrizione |
|---|---|---|
| `VPS_HOST` | `REDACTED-VPS-IP` | IP del VPS Hetzner |
| `VPS_USER` | `asteria` | User SSH sul VPS |
| `CF_ACCOUNT_ID` | `d205b08a93a3a81098cdaabdbcaa1548` | Account Cloudflare |
| `CF_ZONE_ID` | `184fbdcde81f5bb7f9edd4a9eb30fd19` | Zone ID di `asteriacore.online` |
| `CF_TUNNEL_ID` | `4e4aa9aa-46ea-4399-9d71-c2ded67629d1` | Tunnel "asteria" |
| `PROD_DEPLOY_ALLOWLIST` | `["simoleo89","DarkNight97boss"]` | Lista ristretta per i merge su `main`. Usata in `prod-deploy.yml` per verificare `github.actor`. |

### 4.4 Secrets che NON stanno su GitHub

Questi vivono solo sul VPS:

- `/root/.asteria-secrets/db.env` - password MariaDB user `arcturus`
- `/etc/cloudflared/credentials.json` - credenziali del tunnel
- `/etc/asteria/cms-api-prod.env` - config runtime delle API

**Non committarli mai nel repo.** Sono `chmod 600` e leggibili solo da `root` / `asteria`.

---

## 5. Promotion a prod (branch protection)

Configura **Settings -> Branches -> Add branch protection rule** per `main`:

### 5.1 Regole obbligatorie

- [x] **Require a pull request before merging**
  - [x] Require approvals: **1**
  - [x] Dismiss stale pull request approvals when new commits are pushed
  - [x] Require review from Code Owners (se hai un `CODEOWNERS` file)
- [x] **Require status checks to pass before merging**
  - [x] Require branches to be up to date before merging
  - [x] Status check richiesti:
    - i job di `ci.yml` (typecheck, lint, test, build)
    - i check di sicurezza (CodeQL, Semgrep, Gitleaks)
- [x] **Require conversation resolution before merging**
- [x] **Require signed commits** (opzionale ma raccomandato)
- [x] **Require linear history** (no merge commits, solo squash o rebase)
- [x] **Do not allow bypassing the above settings** (anche admin)
- [x] **Restrict who can push to matching branches:** solo `DarkNight97boss` e `simoleo89`

### 5.2 Regole raccomandate

- [ ] **Lock branch** (no, lo lascia editabile)
- [x] **Block force pushes**
- [x] **Block deletions**

### 5.3 Cosa succede al merge

1. La PR viene mergiata su `main` (squash preferito per pulizia history).
2. Il workflow `.github/workflows/prod-deploy.yml` parte.
3. Aspetta l'approval del reviewer (environment `production`).
4. Build con `NODE_ENV=production`.
5. Rsync degli artifact verso `/opt/habboproject`.
6. Lo script `apply-prod.sh`:
   - Applica solo migrations **nuove** (incrementale, tracciato da tabella `schema_migrations`). NON droppa il DB.
   - In-game broadcast `"Riavvio server tra 60 secondi per aggiornamento, salva il tuo lavoro!"` (via comando staff `/broadcast`).
   - Sleep 60 secondi.
   - `systemctl reload-or-restart cms-api` (Hono boot ~150ms, near-zero downtime).
   - `systemctl restart habbo-emu` (5-10s downtime, accettabile).
7. Health check su `https://asteriacore.online` + `/api/health`.
8. Notifica su Discord `#ops-deploy` con SHA, autore merge, lista commit.

---

## 6. Rollback

Tre scenari, in ordine di gravita crescente.

### 6.1 Rollback "soft" - revert della PR

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

Il revert e una nuova PR che a sua volta triggera il flusso completo (CI -> approval -> deploy prod). Tempo totale: ~5 minuti se approvato subito.

### 6.2 Rollback "hard" - redeploy versione precedente direttamente sul VPS

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

### 6.3 Rollback DB (caso peggiore)

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

## 7. Sviluppo locale

Vedi anche `MEMORY.md` (habboproject-local-setup) per i dettagli su porte e gotchas. Quick start:

### 7.1 CMS-V3 frontend (React 19 + Vite 8)

```powershell
cd C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\CMS-V3\apps\web
yarn install
yarn dev
# http://localhost:3000
```

### 7.2 CMS-V3 API (Hono Node)

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

### 7.3 EMU Java

```powershell
cd C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\EMU
# usa JDK 11 oppure JDK 17 (entrambi vanno, vedi MEMORY local-setup)
mvn package -DskipTests
java -jar target/Habbo-3.5.5-jar-with-dependencies.jar
```

### 7.4 Nitro V3 client

```powershell
cd C:\Users\vincenzokatrielgiuva\Desktop\Github\Habboproject\CMS\react
yarn install
yarn build
# l'output dist/ va servito staticamente
```

> **Nota:** lo script `yarn build` del Nitro e bloccato dal classifier. Devi essere **tu** (utente) a lanciarlo manualmente. Vedi `MEMORY.md` (habboproject-nitro-blocked).

### 7.5 Test rapido fullstack senza deploy

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

## 8. Troubleshooting

### 8.1 Prod e giu - dove guardo?

```bash
ssh asteria@REDACTED-VPS-IP
sudo systemctl status habbo-emu cms-api cloudflared nginx mariadb
sudo journalctl -u habbo-emu -n 100 --no-pager
sudo journalctl -u cms-api -n 100 --no-pager
```

Se tutto sembra a posto ma il sito non risponde: **e Cloudflare** (incident upstream). Vai su `https://www.cloudflarestatus.com/`.

### 8.2 Asset Nitro vecchi serviti dal client

Il bundle e cachato su Cloudflare CDN. Force purge:

```bash
# da locale o da CI:
curl -X POST "https://api.cloudflare.com/client/v4/zones/184fbdcde81f5bb7f9edd4a9eb30fd19/purge_cache" \
  -H "Authorization: Bearer $CF_API_TOKEN" \
  -H "Content-Type: application/json" \
  --data '{"purge_everything":true}'
```

Oppure aspetta la TTL (default 4h su asset statici).

---

## 9. Convenzioni di codice

### 9.1 Commit messages (Conventional Commits)

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

### 9.2 PR title

Stessa convention del commit. Il merge usa squash, quindi il PR title diventa il commit message su `main`.

### 9.3 Code style

- **TypeScript:** ESLint + Prettier (config in `CMS-V3/.eslintrc.json`). `yarn lint` prima di pushare.
- **Java:** Google Java Style. IntelliJ ha l'importer pronto.
- **SQL migrations:** una per file, naming `NNN_nome_descrittivo.sql` (es. `042_add_battlepass_progress.sql`). MAI modificare una migration gia applicata in prod - crea sempre una nuova.

### 9.4 Sicurezza

- **MAI committare:** `.env`, `.env.local`, `*.key`, `*.pem`, file con `password`/`secret`/`token` nel nome.
- Il pre-commit hook (se installato via `husky`) blocca questi pattern automaticamente.
- Prima di pushare, scan rapido:
  ```powershell
  git diff --cached | Select-String -Pattern "password|secret|api_key|BEGIN PRIVATE KEY" -CaseSensitive:$false
  ```
- Se hai accidentalmente committato un secret: **non basta rimuoverlo nel commit successivo**. Va rotato immediatamente (genera nuova chiave, aggiorna su VPS + GitHub Secrets) e idealmente rimosso dalla history con `git filter-repo`.

### 9.5 Review checklist (per chi approva)

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
