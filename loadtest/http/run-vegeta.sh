#!/usr/bin/env bash
# Uso: API=http://localhost:18092 ./run-vegeta.sh [rate] [durata]
set -euo pipefail
cd "$(dirname "$0")"
. ./_guard.sh
API="${API:-http://localhost:18092}"; RATE="${1:-200}"; DUR="${2:-30s}"
guard_target "$API" || exit 1
command -v vegeta >/dev/null || { echo "installa vegeta: brew install vegeta"; exit 1; }

sed "s#__API__#${API}#g" targets.txt > /tmp/lt-targets.txt
echo "== GET pesanti (profile/community) @ ${RATE}rps per ${DUR} contro ${API} =="
vegeta attack -targets=/tmp/lt-targets.txt -rate="${RATE}" -duration="${DUR}" \
  | tee /tmp/lt-get.bin | vegeta report

echo; echo "== login flood: deve saturare in 429 (rate-limit sano) =="
sed "s#__API__#${API}#g" login-target.txt > /tmp/lt-login.txt
vegeta attack -targets=/tmp/lt-login.txt -rate="${RATE}" -duration=10s \
  | vegeta report -type='hist[0,10ms,50ms,100ms,250ms,500ms,1s]'
echo "(atteso: pochi 200/401, il resto 429 → il limite regge)"
