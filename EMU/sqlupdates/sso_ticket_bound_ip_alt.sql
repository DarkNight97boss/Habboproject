-- Wave 19 SSO hardening: aggiunge slot secondario per bind_ip dual-stack IPv4/IPv6.
-- Necessario perché happy-eyeballs (RFC 8305) può portare il browser a usare
-- IPv4 per la HTTP request /api/v2/auth/play e IPv6 per il WebSocket (o viceversa).
-- HabboManager applica trust-on-first-use: il primo WS connect dopo il /play
-- sigilla atomicamente bound_ip_alt = peerIp. Successive connessioni devono
-- matchare uno dei due IP. TTL 300s + single-use ticket limitano la finestra
-- di abuso.
ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_ticket_bound_ip_alt VARCHAR(45) DEFAULT NULL AFTER auth_ticket_bound_ip;
