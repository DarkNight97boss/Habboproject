-- =========================================================================
--  CMS-V3 — Daily Streak (Fase 3, feature #1)
--
--  Streak di login giornaliero. Tracciato server-side; il claim premia in
--  crediti via RCON (givecredits) ed emette un activity event 'streak.claimed'.
--  Gestito dietro il feature flag `daily_streak` (OFF di default).
--
--  Una riga per utente (user_id = users.id Arcturus). Niente FK verso `users`
--  (engine/charset upstream non garantiti) — solo PK indicizzata.
-- =========================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `cms_v3_user_streaks` (
    `user_id`         INT(11)   NOT NULL,
    `current_streak`  INT(11)   NOT NULL DEFAULT 0,
    `longest_streak`  INT(11)   NOT NULL DEFAULT 0,
    `last_claim_date` DATE      NULL DEFAULT NULL,
    `total_claims`    INT(11)   NOT NULL DEFAULT 0,
    `updated_at`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
