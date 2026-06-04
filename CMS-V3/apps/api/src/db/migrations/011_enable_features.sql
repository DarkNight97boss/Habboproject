-- 011_enable_features.sql — Fase di SVILUPPO: attiva la suite costruita finora.
--
-- L'utente ha confermato che siamo in fase di sviluppo (pochi/nessun utente
-- reale) con ambiente di produzione predisposto, e di procedere liberamente.
-- Accendiamo quindi i flag delle feature finora in dark-launch (moderazione) +
-- referral, così girano davvero e si possono osservare:
--   • pannello staff → "Alert staff" (raid/macro/minori) e "Feature flag";
--   • /me → sezione "Invita un amico" (referral).
-- Reversibile in qualsiasi momento dal pannello (Feature flag → toggle).
--
-- INSERT...ON DUPLICATE: crea+accende i flag mai seedati (raid_detection,
-- anti_macro, minor_protection) e accende quelli già presenti a 0
-- (shadow_mute, referral). Idempotente; gira una sola volta (tracker).

INSERT INTO `cms_v3_feature_flags` (`key`, `enabled`, `description`, `audience`) VALUES
    ('raid_detection',   1, 'Rilevamento raid/flood in stanza → alert staff', 'all'),
    ('shadow_mute',      1, 'Mute ombra: messaggi visibili solo a mittente e staff', 'all'),
    ('anti_macro',       1, 'Anti-macro: chat a cadenza robotica → alert staff', 'all'),
    ('minor_protection', 1, 'Tutela minori: possibili dichiarazioni d''età → alert staff', 'all'),
    ('referral',         1, 'Referral: codice invito + premio crediti', 'all')
ON DUPLICATE KEY UPDATE `enabled` = VALUES(`enabled`), `description` = VALUES(`description`);
