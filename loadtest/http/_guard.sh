# Sourced dagli script: rifiuta qualsiasi target che non sia locale/privato.
guard_target() {
  local url="$1" host
  host="$(printf '%s' "$url" | sed -E 's#^[a-z]+://##; s#[:/].*$##')"
  case "$host" in
    localhost|127.0.0.1|::1|0.0.0.0) return 0 ;;
    10.*|192.168.*|172.1[6-9].*|172.2[0-9].*|172.3[0-1].*) return 0 ;;
    *asteriacore.online*|*)
      echo "RIFIUTATO: '$host' non è un target locale/privato. Questo strumento" >&2
      echo "punta SOLO a un'istanza usa-e-getta (localhost). Mai la produzione." >&2
      return 1 ;;
  esac
}
