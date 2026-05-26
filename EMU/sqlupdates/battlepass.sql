-- Seasonal Battle Pass (free + premium track, XP-based progression).
-- See com.eu.habbo.core.BattlePass. The current season is auto-created on
-- first server boot if no active row exists. Tier rewards are hardcoded in
-- Java for MVP; they can be moved to a battlepass_tiers table later.

CREATE TABLE IF NOT EXISTS `battlepass_seasons` (
  `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(64) NOT NULL DEFAULT '',
  `start_unix` INT NOT NULL DEFAULT 0,
  `end_unix` INT NOT NULL DEFAULT 0,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  KEY `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `battlepass_progress` (
  `user_id` INT NOT NULL,
  `season_id` INT NOT NULL,
  `xp` INT NOT NULL DEFAULT 0,
  `is_premium` TINYINT(1) NOT NULL DEFAULT 0,
  `claimed_free` VARCHAR(512) NOT NULL DEFAULT '',
  `claimed_premium` VARCHAR(512) NOT NULL DEFAULT '',
  `last_daily_xp_date` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`, `season_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('battlepass.enabled', '1'),
  ('battlepass.xp_per_tier', '500'),
  ('battlepass.premium_cost_diamonds', '50'),
  ('battlepass.xp_daily_login', '50'),
  ('battlepass.xp_streak_claim', '100'),
  ('battlepass.xp_trade_complete', '25'),
  ('battlepass.season_days', '30'),
  ('battlepass.season_name', 'Season 1')
ON DUPLICATE KEY UPDATE `key` = `key`;
