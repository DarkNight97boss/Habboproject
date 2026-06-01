#!/bin/bash
# setup-dev-environment.sh
# ------------------------------------------------------------------------------
# Configura il dev environment Asteria sul VPS (parallelo alla prod, stesso host).
#
# Crea:
#   - DB MariaDB ms_dev + user arcturus_dev
#   - /opt/habboproject-dev (git worktree sparse: EMU + CMS-V3)
#   - /opt/asteria-scripts (apply-dev, apply-prod, apply-sql, backup, rollback, teardown)
#   - /etc/sudoers.d/asteria-deploy (NOPASSWD scoped agli script sopra)
#   - systemd: habbo-emu-dev.service, cms-api-dev.service
#   - nginx: dev.asteriacore.online, dev-api.asteriacore.online, dev-hotel.asteriacore.online
#   - /etc/asteria/cms-api-dev.env, /opt/habboproject-dev/EMU/config.ini
#   - logrotate /etc/logrotate.d/asteria-dev
#
# Idempotente: rieseguibile senza danno. NON tocca i servizi prod.
#
# Esecuzione: sudo bash /opt/habboproject/deploy/setup-dev-environment.sh
# ------------------------------------------------------------------------------

set -euo pipefail

# ============================== CONFIG ========================================
PROD_REPO_PATH="/opt/habboproject"
DEV_REPO_PATH="/opt/habboproject-dev"
SCRIPTS_DIR="/opt/asteria-scripts"
SECRETS_DIR="/root/.asteria-secrets"
BACKUPS_DIR="/opt/asteria-backups"
ASTERIA_USER="asteria"
ASTERIA_GROUP="asteria"
REPO_URL="https://github.com/DarkNight97boss/Habboproject.git"

DEV_DB_NAME="ms_dev"
DEV_DB_USER="arcturus_dev"
DEV_DB_HOST_LOCAL="localhost"
DEV_DB_HOST_TCP="127.0.0.1"

# Porte DEV (prod usa 30000/3001/2096/8092 — dev incrementa di 1)
DEV_EMU_GAME_PORT=30001
DEV_EMU_WEB_PORT=3002
DEV_EMU_WS_PORT=2097
DEV_CMS_API_PORT=8093
DEV_CMS_API_HEALTH_PORT=8093

# Hostnames CF tunnel
DEV_WEB_HOST="dev.asteriacore.online"
DEV_API_HOST="dev-api.asteriacore.online"
DEV_HOTEL_HOST="dev-hotel.asteriacore.online"

LOG_PREFIX="[setup-dev-environment]"

# ============================== HELPERS =======================================
log() { echo "$LOG_PREFIX $(date -u +'%Y-%m-%dT%H:%M:%SZ') $*"; }
warn() { echo "$LOG_PREFIX [WARN] $*" >&2; }
die()  { echo "$LOG_PREFIX [FATAL] $*" >&2; exit 1; }

run_as_asteria() {
    sudo -u "$ASTERIA_USER" -H bash -c "$*"
}

require_root() {
    if [[ "${EUID:-$(id -u)}" -ne 0 ]]; then
        die "Questo script deve girare come root (usa: sudo bash $0)"
    fi
}

require_cmd() {
    command -v "$1" >/dev/null 2>&1 || die "Comando mancante: $1"
}

gen_password() {
    # 32 chars alfanumerici, niente caratteri speciali per evitare quoting hell in .env
    tr -dc 'A-Za-z0-9' </dev/urandom | head -c 32
}

gen_secret() {
    # 64 chars, per JWT/HMAC
    tr -dc 'A-Za-z0-9' </dev/urandom | head -c 64
}

systemd_is_active() {
    systemctl is-active --quiet "$1"
}

# ============================== PREREQUISITES =================================
check_prerequisites() {
    log "Controllo prerequisiti..."
    require_root

    require_cmd git
    require_cmd mysql
    require_cmd nginx
    require_cmd systemctl
    require_cmd rsync
    require_cmd tar

    if ! systemd_is_active mariadb; then
        die "MariaDB non attivo (systemctl start mariadb)"
    fi
    if ! systemd_is_active nginx; then
        die "nginx non attivo (systemctl start nginx)"
    fi

    if [[ ! -d "$PROD_REPO_PATH" ]]; then
        die "Repo prod non trovato in $PROD_REPO_PATH"
    fi

    if [[ ! -d "$SECRETS_DIR" ]]; then
        mkdir -p "$SECRETS_DIR"
        chmod 700 "$SECRETS_DIR"
        log "Creato $SECRETS_DIR"
    fi

    if [[ ! -f "$SECRETS_DIR/db.env" ]]; then
        warn "Prod $SECRETS_DIR/db.env non esiste — il setup procede comunque"
    fi

    # gh CLI per teardown-dev.sh
    if ! command -v gh >/dev/null 2>&1; then
        warn "gh CLI non installato — installo via apt"
        install_gh_cli
    else
        log "gh CLI presente: $(gh --version | head -1)"
    fi

    log "Prerequisiti OK."
}

install_gh_cli() {
    log "Installo gh CLI..."
    type -p curl >/dev/null || apt-get install -y curl
    install -dm 755 /etc/apt/keyrings
    if [[ ! -f /etc/apt/keyrings/githubcli-archive-keyring.gpg ]]; then
        curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg \
            | dd of=/etc/apt/keyrings/githubcli-archive-keyring.gpg
        chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg
    fi
    if [[ ! -f /etc/apt/sources.list.d/github-cli.list ]]; then
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" \
            > /etc/apt/sources.list.d/github-cli.list
    fi
    apt-get update -y
    apt-get install -y gh
}

# ============================== SECRETS GEN ===================================
ensure_secret_file() {
    # ensure_secret_file <path> <key>=<gen_fn> [<key>=<gen_fn> ...]
    # Crea il file se mancante con i secrets generati; se esiste, append delle chiavi mancanti.
    local file="$1"; shift
    local dir
    dir="$(dirname "$file")"
    mkdir -p "$dir"
    chmod 700 "$dir" || true

    touch "$file"
    chmod 600 "$file"
    chown root:root "$file"

    for kv in "$@"; do
        local key="${kv%%=*}"
        local genfn="${kv#*=}"
        if ! grep -q "^${key}=" "$file" 2>/dev/null; then
            local val
            val="$($genfn)"
            echo "${key}=${val}" >> "$file"
            log "  generato secret $key in $file"
        fi
    done
}

setup_secrets() {
    log "Genero/verifico secrets dev..."

    ensure_secret_file "$SECRETS_DIR/db-dev.env" \
        "MARIADB_DEV_PASSWORD=gen_password" \
        "MARIADB_DEV_ROOT_FALLBACK=gen_password"

    ensure_secret_file "$SECRETS_DIR/cms-api-dev.env" \
        "JWT_SECRET=gen_secret" \
        "SESSION_SECRET=gen_secret" \
        "RCON_TOKEN=gen_secret" \
        "ADMIN_BOOTSTRAP_TOKEN=gen_secret"

    # GitHub token read-only per teardown-dev.sh (lasciato vuoto: utente lo riempie a mano)
    if [[ ! -f "$SECRETS_DIR/gh.env" ]]; then
        cat > "$SECRETS_DIR/gh.env" <<'EOF'
# GH_TOKEN read-only fine-grained PAT scoped a contents:read, pull-requests:read.
# Riempire manualmente: gh auth login --with-token < ...
GH_TOKEN=
EOF
        chmod 600 "$SECRETS_DIR/gh.env"
        log "  creato placeholder $SECRETS_DIR/gh.env (riempire GH_TOKEN a mano)"
    fi

    log "Secrets OK."
}

# ============================== DATABASE ======================================
setup_database() {
    log "Configuro DB MariaDB dev..."

    # shellcheck disable=SC1091
    source "$SECRETS_DIR/db-dev.env"
    local pwd="${MARIADB_DEV_PASSWORD:?MARIADB_DEV_PASSWORD non in db-dev.env}"

    # CREATE DATABASE + USER. Idempotente (IF NOT EXISTS + ALTER USER per ruotare pwd se necessario)
    mysql --protocol=socket -u root <<SQL
CREATE DATABASE IF NOT EXISTS \`${DEV_DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DEV_DB_USER}'@'${DEV_DB_HOST_LOCAL}' IDENTIFIED BY '${pwd}';
CREATE USER IF NOT EXISTS '${DEV_DB_USER}'@'${DEV_DB_HOST_TCP}' IDENTIFIED BY '${pwd}';
ALTER USER '${DEV_DB_USER}'@'${DEV_DB_HOST_LOCAL}' IDENTIFIED BY '${pwd}';
ALTER USER '${DEV_DB_USER}'@'${DEV_DB_HOST_TCP}' IDENTIFIED BY '${pwd}';
GRANT ALL PRIVILEGES ON \`${DEV_DB_NAME}\`.* TO '${DEV_DB_USER}'@'${DEV_DB_HOST_LOCAL}';
GRANT ALL PRIVILEGES ON \`${DEV_DB_NAME}\`.* TO '${DEV_DB_USER}'@'${DEV_DB_HOST_TCP}';
FLUSH PRIVILEGES;
SQL

    # Tabella schema_migrations (idempotente, usata da apply-prod.sh ma utile anche in dev per logging)
    mysql --protocol=socket -u root "${DEV_DB_NAME}" <<'SQL'
CREATE TABLE IF NOT EXISTS schema_migrations (
    filename VARCHAR(255) PRIMARY KEY,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(64) NULL
);
SQL

    log "DB ${DEV_DB_NAME} + user ${DEV_DB_USER} OK."
}

# ============================== DEV REPO (sparse worktree) ====================
setup_dev_repo() {
    log "Configuro $DEV_REPO_PATH (sparse: EMU + CMS-V3)..."

    if [[ ! -d "$DEV_REPO_PATH/.git" ]]; then
        log "  clone iniziale..."
        run_as_asteria "git clone --no-checkout '$REPO_URL' '$DEV_REPO_PATH'"
        run_as_asteria "cd '$DEV_REPO_PATH' && git sparse-checkout init --cone"
        run_as_asteria "cd '$DEV_REPO_PATH' && git sparse-checkout set EMU CMS-V3"
        run_as_asteria "cd '$DEV_REPO_PATH' && git checkout main"
    else
        log "  repo gia presente, sincronizzo sparse-checkout..."
        run_as_asteria "cd '$DEV_REPO_PATH' && git sparse-checkout set EMU CMS-V3"
        run_as_asteria "cd '$DEV_REPO_PATH' && git fetch origin main" || warn "git fetch fallito (rete?)"
    fi

    # Ownership corretto
    chown -R "$ASTERIA_USER:$ASTERIA_GROUP" "$DEV_REPO_PATH"

    # Placeholder target dir per il jar EMU che il workflow CI deposita
    mkdir -p "$DEV_REPO_PATH/EMU/target"
    chown "$ASTERIA_USER:$ASTERIA_GROUP" "$DEV_REPO_PATH/EMU/target"

    # Placeholder dist dir per il bundle CMS web
    mkdir -p "$DEV_REPO_PATH/CMS-V3/apps/web/dist"
    chown -R "$ASTERIA_USER:$ASTERIA_GROUP" "$DEV_REPO_PATH/CMS-V3/apps/web/dist"

    log "Dev repo OK."
}

# ============================== EMU CONFIG ====================================
setup_emu_config() {
    log "Scrivo /opt/habboproject-dev/EMU/config.ini..."

    # shellcheck disable=SC1091
    source "$SECRETS_DIR/db-dev.env"
    local db_pwd="${MARIADB_DEV_PASSWORD}"

    local cfg="$DEV_REPO_PATH/EMU/config.ini"
    cat > "$cfg" <<EOF
# ------------------------------------------------------------------------------
# Asteria EMU — DEV environment config (auto-generato da setup-dev-environment.sh)
# NON committare: contiene secrets generati e parametri locali del VPS.
# ------------------------------------------------------------------------------

# Database
db.hostname=127.0.0.1
db.port=3306
db.database=${DEV_DB_NAME}
db.username=${DEV_DB_USER}
db.password=${db_pwd}
db.pool.minsize=5
db.pool.maxsize=20
db.params=useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false

# Game server (TCP nitro/swf legacy)
game.tcp.address=0.0.0.0
game.tcp.port=${DEV_EMU_GAME_PORT}

# WebSocket (nitro V3)
websockets.enabled=true
websockets.ports=${DEV_EMU_WS_PORT}
websockets.whitelist=*

# Webserver interno (RCON + asset bridge)
webserver.enabled=true
webserver.host=127.0.0.1
webserver.port=${DEV_EMU_WEB_PORT}

# RCON
rcon.allowed=127.0.0.1
rcon.port=30002

# Misc
emulator.version=Asteria-DEV-3.5.5
environment=dev
EOF
    chown root:"$ASTERIA_GROUP" "$cfg"
    chmod 640 "$cfg"
    log "  $cfg scritto."
}

# ============================== CMS API ENV ===================================
setup_cms_api_env() {
    log "Scrivo /etc/asteria/cms-api-dev.env..."
    mkdir -p /etc/asteria
    chmod 750 /etc/asteria

    # shellcheck disable=SC1091
    source "$SECRETS_DIR/db-dev.env"
    # shellcheck disable=SC1091
    source "$SECRETS_DIR/cms-api-dev.env"

    local db_pwd="${MARIADB_DEV_PASSWORD}"
    local jwt="${JWT_SECRET}"
    local session="${SESSION_SECRET}"
    local rcon="${RCON_TOKEN}"

    cat > /etc/asteria/cms-api-dev.env <<EOF
# CMS-V3 API — DEV environment (auto-generato)
NODE_ENV=development
PORT=${DEV_CMS_API_PORT}
HOST=127.0.0.1

# Database
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=${DEV_DB_NAME}
DB_USER=${DEV_DB_USER}
DB_PASSWORD=${db_pwd}

# Auth
JWT_SECRET=${jwt}
SESSION_SECRET=${session}
COOKIE_DOMAIN=.asteriacore.online
COOKIE_SAMESITE=Lax
COOKIE_SECURE=true

# EMU bridge
RCON_HOST=127.0.0.1
RCON_PORT=30002
RCON_TOKEN=${rcon}
EMU_WS_URL=ws://127.0.0.1:${DEV_EMU_WS_PORT}

# Frontend / Nitro V3
WEB_HOST=https://${DEV_WEB_HOST}
NITRO_HOST=https://${DEV_HOTEL_HOST}
GAMEDATA_CDN=https://dev-cdn.asteriacore.online
PUBLIC_BASE_URL=https://${DEV_API_HOST}

# R2 (bucket dev separato)
R2_BUCKET=asteria-gamedata-dev
R2_ACCOUNT_ID=d205b08a93a3a81098cdaabdbcaa1548
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_PUBLIC_URL=https://dev-cdn.asteriacore.online

# Feature flags dev
ALLOW_REGISTRATION=true
ENABLE_DEBUG_ROUTES=true
LOG_LEVEL=debug

# CORS
CORS_ORIGIN=https://${DEV_WEB_HOST},http://localhost:5173
EOF

    chown root:"$ASTERIA_GROUP" /etc/asteria/cms-api-dev.env
    chmod 640 /etc/asteria/cms-api-dev.env
    log "  /etc/asteria/cms-api-dev.env scritto."
}

# ============================== SYSTEMD UNITS =================================
setup_systemd_units() {
    log "Scrivo systemd units dev..."

    cat > /etc/systemd/system/habbo-emu-dev.service <<EOF
[Unit]
Description=Asteria Habbo EMU (DEV)
After=network-online.target mariadb.service
Wants=network-online.target
# Avvio dopo prod per non competere a freddo
After=habbo-emu.service

[Service]
Type=simple
User=${ASTERIA_USER}
Group=${ASTERIA_GROUP}
WorkingDirectory=${DEV_REPO_PATH}/EMU
ExecStart=/usr/bin/java -Xms256m -Xmx1536m -jar ${DEV_REPO_PATH}/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar
Restart=on-failure
RestartSec=10s
MemoryMax=2G
StandardOutput=journal
StandardError=journal
SyslogIdentifier=habbo-emu-dev
# Sicurezza
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=full
ProtectHome=true

[Install]
WantedBy=multi-user.target
EOF

    cat > /etc/systemd/system/cms-api-dev.service <<EOF
[Unit]
Description=Asteria CMS-V3 API (DEV)
After=network-online.target mariadb.service
Wants=network-online.target

[Service]
Type=simple
User=${ASTERIA_USER}
Group=${ASTERIA_GROUP}
WorkingDirectory=${DEV_REPO_PATH}/CMS-V3/apps/api
EnvironmentFile=/etc/asteria/cms-api-dev.env
ExecStart=/usr/bin/node dist/server.js
Restart=on-failure
RestartSec=5s
MemoryMax=512M
StandardOutput=journal
StandardError=journal
SyslogIdentifier=cms-api-dev
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=full
ProtectHome=true

[Install]
WantedBy=multi-user.target
EOF

    systemctl daemon-reload
    systemctl enable habbo-emu-dev.service cms-api-dev.service >/dev/null 2>&1 || true

    log "Systemd units (habbo-emu-dev, cms-api-dev) scritti + enabled. NON avvio finche il workflow CI non droppa artifact."
}

# ============================== NGINX =========================================
setup_nginx() {
    log "Scrivo nginx server blocks dev..."

    cat > /etc/nginx/sites-available/dev.asteriacore.online <<EOF
# DEV web (CMS-V3 SPA)
server {
    listen 80;
    listen [::]:80;
    server_name ${DEV_WEB_HOST};

    add_header X-Environment "dev" always;
    add_header X-Robots-Tag "noindex, nofollow, noarchive" always;

    root ${DEV_REPO_PATH}/CMS-V3/apps/web/dist;
    index index.html;

    # SPA fallback
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # Asset cache (ma piu corto in dev)
    location ~* \\.(?:js|css|woff2?|svg|png|jpg|jpeg|gif|webp|ico)\$ {
        expires 1h;
        add_header Cache-Control "public, max-age=3600";
    }

    # Inietta meta noindex via sub_filter (defense in depth oltre header)
    sub_filter '<head>' '<head><meta name="robots" content="noindex,nofollow">';
    sub_filter_once on;
    sub_filter_types text/html;

    access_log /var/log/nginx/dev.access.log;
    error_log  /var/log/nginx/dev.error.log;
}
EOF

    cat > /etc/nginx/sites-available/dev-api.asteriacore.online <<EOF
# DEV API (CMS-V3 Hono)
server {
    listen 80;
    listen [::]:80;
    server_name ${DEV_API_HOST};

    add_header X-Environment "dev" always;
    add_header X-Robots-Tag "noindex, nofollow" always;

    client_max_body_size 25M;

    location / {
        proxy_pass http://127.0.0.1:${DEV_CMS_API_PORT};
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header X-Forwarded-Host \$host;

        # WebSocket upgrade
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    access_log /var/log/nginx/dev-api.access.log;
    error_log  /var/log/nginx/dev-api.error.log;
}
EOF

    cat > /etc/nginx/sites-available/dev-hotel.asteriacore.online <<EOF
# DEV hotel (Nitro V3 gameroom: WS verso EMU + asset bridge)
server {
    listen 80;
    listen [::]:80;
    server_name ${DEV_HOTEL_HOST};

    add_header X-Environment "dev" always;

    # WebSocket nitro
    location /websockets {
        proxy_pass http://127.0.0.1:${DEV_EMU_WS_PORT};
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_read_timeout 86400s;
        proxy_send_timeout 86400s;
    }

    # Asset bridge (EMU webserver)
    location /webserver/ {
        proxy_pass http://127.0.0.1:${DEV_EMU_WEB_PORT}/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }

    # Gamedata statico (Nitro V3 bundle) servito dal dist web del CMS sotto /gamedata/
    location /gamedata/ {
        root ${DEV_REPO_PATH}/CMS-V3/apps/web/dist;
        try_files \$uri =404;
        expires 6h;
    }

    # Fallback al CMS web (per SPA che embed la room)
    location / {
        root ${DEV_REPO_PATH}/CMS-V3/apps/web/dist;
        try_files \$uri /index.html;
    }

    access_log /var/log/nginx/dev-hotel.access.log;
    error_log  /var/log/nginx/dev-hotel.error.log;
}
EOF

    # Symlink in sites-enabled (idempotente)
    for site in dev.asteriacore.online dev-api.asteriacore.online dev-hotel.asteriacore.online; do
        ln -sf "/etc/nginx/sites-available/${site}" "/etc/nginx/sites-enabled/${site}"
    done

    if ! nginx -t; then
        die "nginx -t fallito — controllare /etc/nginx/sites-available/dev*"
    fi
    systemctl reload nginx
    log "nginx server blocks dev + reload OK."
}

# ============================== ASTERIA-SCRIPTS ===============================
setup_asteria_scripts() {
    log "Scrivo $SCRIPTS_DIR/*.sh..."

    mkdir -p "$SCRIPTS_DIR"
    chown root:"$ASTERIA_GROUP" "$SCRIPTS_DIR"
    chmod 750 "$SCRIPTS_DIR"

    # ------------------- apply-sql.sh -----------------------------------------
    cat > "$SCRIPTS_DIR/apply-sql.sh" <<'EOF'
#!/bin/bash
# apply-sql.sh — applica SQL migrations a un DB target.
# Args: $1=DB_NAME $2=DB_USER $3=DB_PWD $4=MODE(reset|tracked) $5=REPO_PATH
# MODE=reset: DROP+CREATE + carica BaseDB + tutti sqlupdates + tutti migrations (no tracking)
# MODE=tracked: salta i file gia presenti in schema_migrations, registra i nuovi
set -euo pipefail
DB_NAME="${1:?}"; DB_USER="${2:?}"; DB_PWD="${3:?}"; MODE="${4:?}"; REPO="${5:?}"

mysql_cmd() { mysql --protocol=socket -u root -D "$DB_NAME" "$@"; }
mysql_root() { mysql --protocol=socket -u root "$@"; }

BASEDB="$REPO/EMU/base database/BaseDB MS 3.5.5.sql"
EMU_UPDATES_DIR="$REPO/EMU/sqlupdates"
CMS_MIG_DIR="$REPO/CMS-V3/apps/api/src/db/migrations"

if [[ "$MODE" == "reset" ]]; then
    echo "[apply-sql] RESET $DB_NAME"
    mysql_root -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    mysql_root -e "GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'localhost'; GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'127.0.0.1'; FLUSH PRIVILEGES;"

    if [[ -f "$BASEDB" ]]; then
        echo "[apply-sql] import BaseDB"
        mysql_cmd < "$BASEDB"
    else
        echo "[apply-sql] WARN BaseDB non trovato in $BASEDB"
    fi

    mysql_cmd <<'SQL'
CREATE TABLE IF NOT EXISTS schema_migrations (
    filename VARCHAR(255) PRIMARY KEY,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(64) NULL
);
SQL

    apply_dir() {
        local d="$1"
        [[ -d "$d" ]] || { echo "[apply-sql] skip dir mancante $d"; return 0; }
        find "$d" -maxdepth 1 -type f -name '*.sql' | LC_ALL=C sort | while read -r f; do
            echo "[apply-sql] apply $f"
            mysql_cmd < "$f"
            local fname; fname="$(basename "$f")"
            mysql_cmd -e "INSERT IGNORE INTO schema_migrations (filename) VALUES ('$fname');"
        done
    }
    apply_dir "$EMU_UPDATES_DIR"
    apply_dir "$CMS_MIG_DIR"

    # Seed staff dev
    mysql_cmd <<'SQL'
INSERT IGNORE INTO users (username, password, mail, account_created, rank)
VALUES ('devstaff', '$2a$10$placeholderhashreplaceonloginflowbcryptcost10aaaaaaaaaaaaaaaaa', 'dev@asteriacore.online', UNIX_TIMESTAMP(), 7);
SQL

elif [[ "$MODE" == "tracked" ]]; then
    echo "[apply-sql] TRACKED apply to $DB_NAME"
    mysql_cmd <<'SQL'
CREATE TABLE IF NOT EXISTS schema_migrations (
    filename VARCHAR(255) PRIMARY KEY,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(64) NULL
);
SQL

    apply_tracked() {
        local d="$1"
        [[ -d "$d" ]] || { echo "[apply-sql] skip dir mancante $d"; return 0; }
        find "$d" -maxdepth 1 -type f -name '*.sql' | LC_ALL=C sort | while read -r f; do
            local fname; fname="$(basename "$f")"
            local exists
            exists="$(mysql_cmd -Nse "SELECT COUNT(*) FROM schema_migrations WHERE filename='$fname'")"
            if [[ "$exists" == "0" ]]; then
                echo "[apply-sql] apply $fname"
                mysql_cmd < "$f"
                mysql_cmd -e "INSERT INTO schema_migrations (filename) VALUES ('$fname');"
            else
                echo "[apply-sql] skip $fname (gia applicato)"
            fi
        done
    }
    apply_tracked "$EMU_UPDATES_DIR"
    apply_tracked "$CMS_MIG_DIR"

else
    echo "[apply-sql] MODE sconosciuto: $MODE (atteso reset|tracked)" >&2
    exit 2
fi

echo "[apply-sql] done"
EOF

    # ------------------- apply-dev.sh -----------------------------------------
    cat > "$SCRIPTS_DIR/apply-dev.sh" <<'EOF'
#!/bin/bash
# apply-dev.sh — deploy DEV per una PR.
# Args: $1=SHA $2=PR_NUMBER $3=FLAGS_EMU $4=FLAGS_CMSWEB $5=FLAGS_CMSAPI $6=FLAGS_SQL $7=FLAGS_NITRO
# Artifact attesi in /tmp/asteria-dev-$SHA/
set -euo pipefail

SHA="${1:?SHA mancante}"
PR="${2:?PR_NUMBER mancante}"
F_EMU="${3:-false}"
F_CMSWEB="${4:-false}"
F_CMSAPI="${5:-false}"
F_SQL="${6:-false}"
F_NITRO="${7:-false}"

REPO=/opt/habboproject-dev
ART=/tmp/asteria-dev-$SHA
SCRIPTS=/opt/asteria-scripts
SECRETS=/root/.asteria-secrets

log() { echo "[apply-dev $(date -u +%H:%M:%S)] $*"; }

log "PR #$PR SHA=$SHA flags emu=$F_EMU cmsweb=$F_CMSWEB cmsapi=$F_CMSAPI sql=$F_SQL nitro=$F_NITRO"

# a) git fetch + checkout
log "git fetch PR ref"
sudo -u asteria -H git -C "$REPO" fetch origin "pull/$PR/head:pr-$PR" --force
sudo -u asteria -H git -C "$REPO" checkout "$SHA"

# b) SQL reset
if [[ "$F_SQL" == "true" ]]; then
    log "SQL reset ms_dev"
    # shellcheck disable=SC1091
    source "$SECRETS/db-dev.env"
    "$SCRIPTS/apply-sql.sh" "ms_dev" "arcturus_dev" "$MARIADB_DEV_PASSWORD" "reset" "$REPO"
fi

# c) EMU jar
if [[ "$F_EMU" == "true" ]]; then
    log "EMU drop jar + restart"
    if [[ ! -f "$ART/emu-jar/Habbo-3.5.5-jar-with-dependencies.jar" ]]; then
        echo "[apply-dev] FATAL artifact EMU jar mancante in $ART/emu-jar/" >&2
        exit 3
    fi
    cp -f "$ART/emu-jar/Habbo-3.5.5-jar-with-dependencies.jar" "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar"
    chown asteria:asteria "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar"
    systemctl restart habbo-emu-dev.service
fi

# d) CMS web dist
if [[ "$F_CMSWEB" == "true" ]]; then
    log "CMS web rsync dist"
    if [[ ! -d "$ART/cms-web-dist" ]]; then
        echo "[apply-dev] FATAL artifact cms-web-dist mancante in $ART/" >&2
        exit 3
    fi
    rsync -a --delete "$ART/cms-web-dist/" "$REPO/CMS-V3/apps/web/dist/"
    chown -R asteria:asteria "$REPO/CMS-V3/apps/web/dist"
    nginx -t && systemctl reload nginx
fi

# e) CMS api bundle
if [[ "$F_CMSAPI" == "true" ]]; then
    log "CMS api extract + restart"
    if [[ ! -f "$ART/cms-api-bundle.tar.gz" ]]; then
        echo "[apply-dev] FATAL artifact cms-api-bundle.tar.gz mancante in $ART/" >&2
        exit 3
    fi
    tar -xzf "$ART/cms-api-bundle.tar.gz" -C "$REPO/CMS-V3/apps/api/"
    chown -R asteria:asteria "$REPO/CMS-V3/apps/api"
    systemctl restart cms-api-dev.service
fi

# f) healthcheck (30s)
log "healthcheck :8093/health"
for i in $(seq 1 30); do
    if curl -fsS --max-time 2 "http://127.0.0.1:8093/health" >/dev/null 2>&1; then
        log "healthcheck OK dopo ${i}s"
        break
    fi
    sleep 1
    if [[ $i -eq 30 ]]; then
        echo "[apply-dev] WARN healthcheck NON OK dopo 30s — il workflow puo segnare PR con warn"
    fi
done

# g) cleanup
log "cleanup $ART"
rm -rf "$ART" || true

log "DONE PR #$PR -> https://dev.asteriacore.online"
EOF

    # ------------------- apply-prod.sh ----------------------------------------
    cat > "$SCRIPTS_DIR/apply-prod.sh" <<'EOF'
#!/bin/bash
# apply-prod.sh — deploy PROD atomico per un merge su main.
# Args: $1=SHA $2=FLAGS_EMU $3=FLAGS_CMSWEB $4=FLAGS_CMSAPI $5=FLAGS_SQL $6=FLAGS_NITRO
# Artifact attesi in /tmp/asteria-prod-$SHA/
# NO drop DB — migrazioni tracked via schema_migrations.
set -euo pipefail

SHA="${1:?SHA mancante}"
F_EMU="${2:-false}"
F_CMSWEB="${3:-false}"
F_CMSAPI="${4:-false}"
F_SQL="${5:-false}"
F_NITRO="${6:-false}"

REPO=/opt/habboproject
ART=/tmp/asteria-prod-$SHA
SCRIPTS=/opt/asteria-scripts
SECRETS=/root/.asteria-secrets

log() { echo "[apply-prod $(date -u +%H:%M:%S)] $*"; }

log "PROD SHA=$SHA flags emu=$F_EMU cmsweb=$F_CMSWEB cmsapi=$F_CMSAPI sql=$F_SQL nitro=$F_NITRO"

# Backup pre-deploy
log "backup pre-deploy"
"$SCRIPTS/backup-prod.sh" "$SHA" || { echo "[apply-prod] FATAL backup fallito — abort"; exit 4; }

# git pull (prod segue main)
log "git fetch+checkout main"
sudo -u asteria -H git -C "$REPO" fetch origin main
sudo -u asteria -H git -C "$REPO" checkout "$SHA"

# SQL tracked
if [[ "$F_SQL" == "true" ]]; then
    log "SQL tracked migrations ms"
    # shellcheck disable=SC1091
    source "$SECRETS/db.env"
    DB_PWD="${MARIADB_PASSWORD:-${MYSQL_PASSWORD:-}}"
    [[ -n "$DB_PWD" ]] || { echo "[apply-prod] FATAL password DB prod non trovata"; exit 5; }
    "$SCRIPTS/apply-sql.sh" "ms" "arcturus" "$DB_PWD" "tracked" "$REPO"
fi

# EMU jar (atomic: scrivi .new poi mv)
if [[ "$F_EMU" == "true" ]]; then
    log "EMU atomic jar swap + restart"
    cp -f "$ART/emu-jar/Habbo-3.5.5-jar-with-dependencies.jar" "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar.new"
    mv -f "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar.new" "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar"
    chown asteria:asteria "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar"
    systemctl restart habbo-emu.service
fi

# CMS web (blue-green ridotto)
if [[ "$F_CMSWEB" == "true" ]]; then
    log "CMS web blue-green"
    WEB_DIR="$REPO/CMS-V3/apps/web"
    rsync -a --delete "$ART/cms-web-dist/" "$WEB_DIR/dist-new/"
    chown -R asteria:asteria "$WEB_DIR/dist-new"
    # swap atomic via symlink se possibile, altrimenti rsync into dist con --delete
    if [[ -L "$WEB_DIR/dist" ]] || [[ ! -e "$WEB_DIR/dist" ]]; then
        ln -sfn "$WEB_DIR/dist-new" "$WEB_DIR/dist.tmp"
        mv -Tf "$WEB_DIR/dist.tmp" "$WEB_DIR/dist"
    else
        rsync -a --delete "$WEB_DIR/dist-new/" "$WEB_DIR/dist/"
        rm -rf "$WEB_DIR/dist-new"
    fi
    nginx -t && systemctl reload nginx
fi

# CMS api blue-green ridotto
if [[ "$F_CMSAPI" == "true" ]]; then
    log "CMS api extract + restart"
    API_DIR="$REPO/CMS-V3/apps/api"
    tar -xzf "$ART/cms-api-bundle.tar.gz" -C "$API_DIR/"
    chown -R asteria:asteria "$API_DIR"
    systemctl restart cms-api.service
fi

# healthcheck prod :8092
log "healthcheck :8092/health"
ok=0
for i in $(seq 1 60); do
    if curl -fsS --max-time 2 "http://127.0.0.1:8092/health" >/dev/null 2>&1; then
        ok=1
        log "healthcheck OK dopo ${i}s"
        break
    fi
    sleep 1
done
if [[ $ok -ne 1 ]]; then
    echo "[apply-prod] FATAL healthcheck KO — eseguire rollback-prod.sh"
    exit 6
fi

rm -rf "$ART" || true
log "DONE PROD -> https://asteriacore.online"
EOF

    # ------------------- backup-prod.sh ---------------------------------------
    cat > "$SCRIPTS_DIR/backup-prod.sh" <<'EOF'
#!/bin/bash
# backup-prod.sh — snapshot pre-deploy prod: dump DB + tar jar EMU + tar dist CMS.
# Args: $1=TAG (opzionale, default timestamp)
set -euo pipefail

TAG="${1:-$(date -u +%Y%m%dT%H%M%SZ)}"
OUT=/opt/asteria-backups
REPO=/opt/habboproject
SECRETS=/root/.asteria-secrets

mkdir -p "$OUT"

# DB
# shellcheck disable=SC1091
source "$SECRETS/db.env" 2>/dev/null || true

echo "[backup] mysqldump ms -> $OUT/db-${TAG}.sql.gz"
mysqldump --single-transaction --quick --routines --triggers --events --hex-blob \
    --default-character-set=utf8mb4 \
    -u root ms | gzip -9 > "$OUT/db-${TAG}.sql.gz"

# EMU jar
if [[ -f "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar" ]]; then
    cp -f "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar" "$OUT/emu-${TAG}.jar"
fi

# CMS web dist
if [[ -d "$REPO/CMS-V3/apps/web/dist" ]]; then
    tar -czf "$OUT/cms-web-${TAG}.tar.gz" -C "$REPO/CMS-V3/apps/web" dist
fi

# CMS api dist
if [[ -d "$REPO/CMS-V3/apps/api/dist" ]]; then
    tar -czf "$OUT/cms-api-${TAG}.tar.gz" -C "$REPO/CMS-V3/apps/api" dist
fi

# Retention 14 giorni
find "$OUT" -maxdepth 1 -type f -mtime +14 -print -delete

echo "[backup] done TAG=$TAG"
EOF

    # ------------------- rollback-prod.sh -------------------------------------
    cat > "$SCRIPTS_DIR/rollback-prod.sh" <<'EOF'
#!/bin/bash
# rollback-prod.sh — ripristina l'ultimo backup (DB + jar + dist).
# Args: $1=TAG (opzionale, default = ultimo per timestamp)
set -euo pipefail

OUT=/opt/asteria-backups
REPO=/opt/habboproject

TAG="${1:-}"
if [[ -z "$TAG" ]]; then
    TAG="$(ls -1 "$OUT" | grep -oE 'db-[0-9TZ]+\.sql\.gz' | sed 's/^db-//;s/\.sql\.gz$//' | sort -r | head -1)"
    [[ -n "$TAG" ]] || { echo "[rollback] nessun backup trovato"; exit 1; }
fi

echo "[rollback] TAG=$TAG"

# Stop services
systemctl stop cms-api.service habbo-emu.service

# DB restore
if [[ -f "$OUT/db-${TAG}.sql.gz" ]]; then
    echo "[rollback] DB restore"
    gunzip -c "$OUT/db-${TAG}.sql.gz" | mysql -u root ms
fi

# EMU jar
if [[ -f "$OUT/emu-${TAG}.jar" ]]; then
    cp -f "$OUT/emu-${TAG}.jar" "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar"
    chown asteria:asteria "$REPO/EMU/target/Habbo-3.5.5-jar-with-dependencies.jar"
fi

# CMS web
if [[ -f "$OUT/cms-web-${TAG}.tar.gz" ]]; then
    rm -rf "$REPO/CMS-V3/apps/web/dist"
    tar -xzf "$OUT/cms-web-${TAG}.tar.gz" -C "$REPO/CMS-V3/apps/web"
    chown -R asteria:asteria "$REPO/CMS-V3/apps/web/dist"
fi

# CMS api
if [[ -f "$OUT/cms-api-${TAG}.tar.gz" ]]; then
    rm -rf "$REPO/CMS-V3/apps/api/dist"
    tar -xzf "$OUT/cms-api-${TAG}.tar.gz" -C "$REPO/CMS-V3/apps/api"
    chown -R asteria:asteria "$REPO/CMS-V3/apps/api/dist"
fi

# Restart
systemctl start habbo-emu.service cms-api.service
nginx -t && systemctl reload nginx

echo "[rollback] done — verifica https://asteriacore.online"
EOF

    # ------------------- teardown-dev.sh --------------------------------------
    cat > "$SCRIPTS_DIR/teardown-dev.sh" <<'EOF'
#!/bin/bash
# teardown-dev.sh — pulizia ambiente dev:
#   - rm /tmp/asteria-dev-* piu vecchi di 7 giorni
#   - se nessuna PR aperta (gh API), stop habbo-emu-dev + cms-api-dev (risparmio RAM)
set -euo pipefail

SECRETS=/root/.asteria-secrets

# Cleanup tmp
find /tmp -maxdepth 1 -name 'asteria-dev-*' -type d -mtime +7 -exec rm -rf {} \; 2>/dev/null || true

# GH check
if [[ -f "$SECRETS/gh.env" ]]; then
    # shellcheck disable=SC1091
    source "$SECRETS/gh.env"
fi

if [[ -z "${GH_TOKEN:-}" ]]; then
    echo "[teardown-dev] GH_TOKEN vuoto — skip stop services (lasciali up)"
    exit 0
fi

OPEN_PRS="$(GH_TOKEN="$GH_TOKEN" gh pr list --repo DarkNight97boss/Habboproject --state open --json number --jq 'length' 2>/dev/null || echo 0)"
echo "[teardown-dev] PR aperte: $OPEN_PRS"

if [[ "$OPEN_PRS" -eq 0 ]]; then
    echo "[teardown-dev] nessuna PR open — stop dev services"
    systemctl stop habbo-emu-dev.service cms-api-dev.service || true
else
    echo "[teardown-dev] PR attive — dev services restano up"
fi
EOF

    # Permessi & ownership su tutti gli script
    for f in apply-sql.sh apply-dev.sh apply-prod.sh backup-prod.sh rollback-prod.sh teardown-dev.sh; do
        chown root:root "$SCRIPTS_DIR/$f"
        chmod 0750 "$SCRIPTS_DIR/$f"
    done

    # Backup dir
    mkdir -p "$BACKUPS_DIR"
    chown root:"$ASTERIA_GROUP" "$BACKUPS_DIR"
    chmod 750 "$BACKUPS_DIR"

    log "Scripts in $SCRIPTS_DIR/ OK."
}

# ============================== SUDOERS =======================================
setup_sudoers() {
    log "Scrivo /etc/sudoers.d/asteria-deploy..."

    local tmpfile
    tmpfile="$(mktemp)"
    cat > "$tmpfile" <<EOF
# Asteria deploy — NOPASSWD scoped agli script in $SCRIPTS_DIR
# (asteria ha gia NOPASSWD globale; questo file e ready se in futuro lo si revoca.)
$ASTERIA_USER ALL=(root) NOPASSWD: $SCRIPTS_DIR/apply-dev.sh, $SCRIPTS_DIR/apply-prod.sh, $SCRIPTS_DIR/apply-sql.sh, $SCRIPTS_DIR/backup-prod.sh, $SCRIPTS_DIR/rollback-prod.sh, $SCRIPTS_DIR/teardown-dev.sh
EOF

    # visudo -c valida
    if visudo -cf "$tmpfile" >/dev/null; then
        mv "$tmpfile" /etc/sudoers.d/asteria-deploy
        chmod 0440 /etc/sudoers.d/asteria-deploy
        chown root:root /etc/sudoers.d/asteria-deploy
        log "  /etc/sudoers.d/asteria-deploy installato + validato."
    else
        rm -f "$tmpfile"
        die "visudo validation fallita per /etc/sudoers.d/asteria-deploy"
    fi
}

# ============================== LOGROTATE =====================================
setup_logrotate() {
    log "Scrivo /etc/logrotate.d/asteria-dev..."
    cat > /etc/logrotate.d/asteria-dev <<EOF
${DEV_REPO_PATH}/EMU/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    copytruncate
    su ${ASTERIA_USER} ${ASTERIA_GROUP}
}

/var/log/nginx/dev*.log /var/log/nginx/dev*.access.log /var/log/nginx/dev*.error.log {
    daily
    rotate 14
    compress
    delaycompress
    missingok
    notifempty
    sharedscripts
    postrotate
        systemctl reload nginx >/dev/null 2>&1 || true
    endscript
}
EOF
    log "logrotate OK."
}

# ============================== TEARDOWN CRON =================================
setup_teardown_cron() {
    log "Scrivo /etc/cron.d/asteria-teardown-dev (daily 04:30 UTC)..."
    cat > /etc/cron.d/asteria-teardown-dev <<EOF
# Asteria teardown dev — daily cleanup + stop services se nessuna PR
SHELL=/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
30 4 * * * root $SCRIPTS_DIR/teardown-dev.sh >> /var/log/asteria-teardown-dev.log 2>&1
EOF
    chmod 644 /etc/cron.d/asteria-teardown-dev
    log "cron OK."
}

# ============================== OUTPUT SUMMARY ================================
print_summary() {
    log "============================================================"
    log "SETUP DEV COMPLETATO."
    log "============================================================"

    echo
    echo "=== File creati ==="
    ls -la "$SCRIPTS_DIR/" 2>/dev/null | sed 's/^/  /'
    echo
    ls -la "$SECRETS_DIR/" 2>/dev/null | sed 's/^/  /'
    echo
    echo "=== Configs ==="
    ls -la /etc/asteria/cms-api-dev.env "$DEV_REPO_PATH/EMU/config.ini" 2>/dev/null | sed 's/^/  /'
    echo
    ls -la /etc/systemd/system/habbo-emu-dev.service /etc/systemd/system/cms-api-dev.service 2>/dev/null | sed 's/^/  /'
    echo
    ls -la /etc/nginx/sites-enabled/dev*.asteriacore.online 2>/dev/null | sed 's/^/  /'
    echo
    ls -la /etc/sudoers.d/asteria-deploy /etc/logrotate.d/asteria-dev /etc/cron.d/asteria-teardown-dev 2>/dev/null | sed 's/^/  /'
    echo

    echo "=== systemctl status (4 services) ==="
    for svc in cms-api.service habbo-emu.service cms-api-dev.service habbo-emu-dev.service; do
        echo "--- $svc ---"
        systemctl status "$svc" --no-pager --lines=3 2>&1 | head -10 | sed 's/^/  /'
        echo
    done

    echo "=== DB check ==="
    mysql --protocol=socket -u root -e "SHOW DATABASES LIKE 'ms%';" 2>&1 | sed 's/^/  /'
    mysql --protocol=socket -u root -e "SELECT user, host FROM mysql.user WHERE user LIKE 'arcturus%';" 2>&1 | sed 's/^/  /'
    echo

    echo "=== Porte in ascolto (dev) ==="
    (ss -tlnp 2>/dev/null || netstat -tlnp 2>/dev/null) | grep -E ":(${DEV_EMU_GAME_PORT}|${DEV_EMU_WEB_PORT}|${DEV_EMU_WS_PORT}|${DEV_CMS_API_PORT})\b" | sed 's/^/  /' || echo "  (nessuna porta dev attiva — normale finche CI non droppa artifact)"
    echo

    cat <<EOF

============================================================
NEXT STEPS (manuali, fuori scope di questo script):
============================================================
1. Cloudflare Tunnel: aggiungi Public Hostnames al tunnel "asteria":
     dev.asteriacore.online        -> http://localhost:80
     dev-api.asteriacore.online    -> http://localhost:80
     dev-hotel.asteriacore.online  -> http://localhost:80
   (oppure modifica /etc/cloudflared/config.yml + systemctl restart cloudflared)

2. Cloudflare DNS: assicurati che i 3 hostname dev.* puntino al tunnel (CNAME proxied).

3. R2: crea bucket asteria-gamedata-dev + custom domain dev-cdn.asteriacore.online.
   Popola R2_ACCESS_KEY_ID/R2_SECRET_ACCESS_KEY in /etc/asteria/cms-api-dev.env.

4. GitHub secrets repo (per il workflow CI):
     SSH_PRIVATE_KEY        -> chiave ssh asteria@VPS
     CF_API_TOKEN           -> scoped al tunnel asteria
     R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY
     VPS_HOST=REDACTED-VPS-IP, VPS_USER=asteria

5. GH token read-only: riempi /root/.asteria-secrets/gh.env con GH_TOKEN scoped a:
     contents:read, pull-requests:read (per teardown-dev.sh)

6. Workflow GitHub Actions (in .github/workflows/) deve:
   - on PR opened/sync verso main + author whitelist -> build artifact -> scp /tmp/asteria-dev-\$SHA/ -> ssh asteria@vps "sudo $SCRIPTS_DIR/apply-dev.sh \$SHA \$PR ..."
   - on push main (post-merge) -> build -> scp /tmp/asteria-prod-\$SHA/ -> ssh asteria@vps "sudo $SCRIPTS_DIR/apply-prod.sh \$SHA ..."

7. Primo deploy dev: trigger manuale workflow su una PR aperta per popolare jar + dist.

============================================================
EOF

    log "DONE. Riesegui questo script in qualsiasi momento (idempotente)."
}

# ============================== MAIN ==========================================
main() {
    log "Avvio setup dev environment Asteria."

    check_prerequisites
    setup_secrets
    setup_database
    setup_dev_repo
    setup_emu_config
    setup_cms_api_env
    setup_systemd_units
    setup_nginx
    setup_asteria_scripts
    setup_sudoers
    setup_logrotate
    setup_teardown_cron

    print_summary
}

main "$@"
