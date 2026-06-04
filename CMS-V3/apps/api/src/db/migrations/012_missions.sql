-- 012_missions.sql — Missioni giornaliere/settimanali (#3).
--
-- Le missioni sono definite in codice (routes/missions.ts). Il progresso si
-- calcola contando gli activity event dell'utente (cms_v3_activity_events,
-- attribuiti via actor_id) nel periodo corrente — nessuna nuova instrumentazione.
-- Questa tabella tiene solo i RISCATTI (un riscatto per missione/periodo).
--
-- Fase sviluppo → flag `missions` acceso di default. Idempotente.

CREATE TABLE IF NOT EXISTS `cms_v3_mission_claims` (
    `user_id`    INT          NOT NULL,
    `mission_id` VARCHAR(48)  NOT NULL,          -- id missione (codice)
    `period_key` VARCHAR(16)  NOT NULL,          -- es. '2026-06-04' (daily) o lunedì della settimana
    `reward`     INT          NOT NULL DEFAULT 0,
    `claimed_at` BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `mission_id`, `period_key`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `cms_v3_feature_flags` (`key`, `enabled`, `description`, `audience`) VALUES
    ('missions', 1, 'Missioni giornaliere/settimanali con ricompense in crediti', 'all')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);
