-- Fix audit 2026-10 (H20): PATCH /me/privacy scrive users.cms_currency_private,
-- colonna definita solo in EMU/sqlupdates/cms_compat_v3.sql che in prod e' stato
-- baseline-ato nel tracker senza essere eseguito (stesso caso di `hidden`, vedi 020).
-- Idempotente.
ALTER TABLE users ADD COLUMN IF NOT EXISTS cms_currency_private TINYINT(1) NOT NULL DEFAULT 0;
