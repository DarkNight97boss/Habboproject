-- 019_sanctions_ladder.sql — #18 Scala sanzioni (dark-launch OSSERVAZIONALE).
--
-- Aggiunge il flag `sanctions_ladder` (OFF di default). Quando attivo, l'ingest
-- eventi (POST /api/v2/activity/ingest) conta le segnalazioni di moderazione
-- recenti (raid/macro/minor/toxicity, finestra 24h) per utente e, al superamento
-- di una soglia (3→warn, 6→mute, 10→kick, 15→ban), emette un evento staff
-- `sanction.suggested` con l'azione SUGGERITA. NON applica MAI sanzioni: solo
-- segnalazione, lo staff decide. Reversibile dal pannello (Feature flag → toggle).
-- Idempotente; gira una sola volta (tracker schema_migrations).

INSERT INTO `cms_v3_feature_flags` (`key`, `enabled`, `description`, `audience`) VALUES
    ('sanctions_ladder', 0, 'Scala sanzioni: troppe segnalazioni moderazione → suggerisce un''azione allo staff (non applica)', 'staff')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);
