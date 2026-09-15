-- =========================================================================
-- SSO ticket hardening (Wave 18): bind del ticket a IP + User-Agent.
--
-- Aggiunge 2 colonne alla tabella users:
--   auth_ticket_bound_ip   : IP del client al momento di /play (IPv4/IPv6)
--   auth_ticket_ua_hash    : SHA-256 dell'User-Agent al momento di /play
--
-- Il CMS-V3 (apps/api/src/routes/auth.ts) popola entrambe le colonne quando
-- emette un ticket. L'EMU (HabboManager.loadHabbo) le confronta con il peer
-- IP del WebSocket e (se inviato) con l'UA-hash del client al login.
--
-- Mismatch → ticket rifiutato (anche se non scaduto, anche se single-use ok)
-- → log audit + null returned → utente vede errore di login.
--
-- Idempotente: usa "IF NOT EXISTS" per i column add (MariaDB 10.5+).
-- =========================================================================

SET NAMES utf8mb4;

ALTER TABLE `users`
    ADD COLUMN IF NOT EXISTS `auth_ticket_bound_ip` VARCHAR(45) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS `auth_ticket_ua_hash`  CHAR(64)    NOT NULL DEFAULT '' AFTER `auth_ticket_bound_ip`;

-- Pulizia ticket esistenti che NON hanno IP/UA bound (issued prima di questa
-- migration). Sono validi ma a vita potenzialmente illimitata se l'EMU non
-- ha ancora il check IP. Per sicurezza azzeriamo tutto: gli utenti dovranno
-- fare un nuovo /play.
UPDATE `users`
SET `auth_ticket` = '',
    `auth_ticket_issued_at` = 0,
    `auth_ticket_bound_ip` = '',
    `auth_ticket_ua_hash` = ''
WHERE `auth_ticket_bound_ip` = '' AND `auth_ticket` <> '';
