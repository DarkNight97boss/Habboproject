-- Fix schema drift PROD (pentest 2026-09-20).
-- La colonna users.hidden e' definita in EMU/sqlupdates/cms_compat_v3.sql ma NON
-- nella BaseDB 3.5.5; su produzione non e' mai stata applicata, mentre il codice
-- (dal #180) la legge in GET /profile/:username -> il SELECT falliva -> 500 per
-- ogni profilo (anche inesistente). Dev funzionava perche' il suo DB e' stato
-- resettato con tutti gli sqlupdates. Idempotente: no-op dove la colonna esiste.
ALTER TABLE users ADD COLUMN IF NOT EXISTS hidden TINYINT(1) NOT NULL DEFAULT 0;
