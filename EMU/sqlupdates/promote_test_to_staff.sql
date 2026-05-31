-- =========================================================================
-- Promuove un account a STAFF (rank 7 = admin) per testare il pannello
-- /admin del CMS-V3.
--
-- Convention Arcturus stock:
--   rank 1   = user normale
--   rank 2-4 = VIP / vari
--   rank 5+  = staff (passa requireRank(5) sul CMS-V3 backend)
--   rank 7   = administrator (full power in housekeeping)
--   rank 10  = owner
--
-- L'utente DEVE poi rieffettuare il login per ottenere un nuovo JWT
-- access token che embedda il nuovo rank (i token precedenti continuano
-- a portare il vecchio rank fino a scadenza — 15 min).
-- =========================================================================

-- Sostituisci 'test' con l'username target.
UPDATE users SET rank = 7 WHERE username = 'test' LIMIT 1;

-- Verifica:
SELECT id, username, rank FROM users WHERE username = 'test';
