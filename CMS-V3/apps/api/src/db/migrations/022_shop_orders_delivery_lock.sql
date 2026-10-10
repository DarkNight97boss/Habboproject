-- Fix audit 2026-10 (H13/H14): lock di consegna per il webhook Stripe.
-- Permette di ritentare la consegna (retry Stripe) se una precedente è morta a metà,
-- senza doppia erogazione concorrente. Idempotente.
ALTER TABLE cms_v3_shop_orders ADD COLUMN IF NOT EXISTS delivery_lock INT(11) NULL DEFAULT NULL;
