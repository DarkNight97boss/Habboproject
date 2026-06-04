-- 013_season_pass.sql — Battle Pass / Season Pass (#2), v1.
--
-- L'XP della stagione si calcola sommando (pesato) gli activity event
-- dell'utente dall'inizio stagione (cms_v3_activity_events, via actor_id) —
-- stesso approccio delle missioni, nessuna nuova instrumentazione. Questa
-- tabella tiene solo i RISCATTI per tier (un riscatto per tier/stagione).
--
-- Il flag `season_pass` esiste già (seedato in 006, attualmente ON). Idempotente.

CREATE TABLE IF NOT EXISTS `cms_v3_season_pass_claims` (
    `user_id`    INT          NOT NULL,
    `season`     VARCHAR(16)  NOT NULL,          -- id stagione (es. 'S1')
    `tier`       INT          NOT NULL,
    `reward`     INT          NOT NULL DEFAULT 0,
    `claimed_at` BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `season`, `tier`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
