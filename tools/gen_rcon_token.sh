#!/usr/bin/env bash
# gen_rcon_token.sh
# Genera un token RCON criptograficamente sicuro (64 hex = 256 bit).
# Lo stesso valore va messo in EMU/config.ini e API/config.php.
#
# Uso:  ./tools/gen_rcon_token.sh

set -euo pipefail

TOKEN=$(openssl rand -hex 32)

cat <<EOF

=== RCON token (256 bit, hex) ===
$TOKEN

Aggiorna:
  EMU/config.ini       -> rcon.token=$TOKEN
  API/config.php       -> \$RCON_TOKEN = '$TOKEN';

Riavvia l'emulatore e l'API dopo aver applicato.
EOF
