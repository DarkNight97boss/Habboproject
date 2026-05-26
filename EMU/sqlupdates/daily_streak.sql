-- Daily login streak with cycling 7-day rewards + milestone bonuses.
-- See com.eu.habbo.core.DailyStreak. Feature is on by default but config-gated
-- via emulator_settings (daily_streak.enabled).

CREATE TABLE IF NOT EXISTS `daily_streak` (
  `user_id` INT NOT NULL,
  `current_streak` INT NOT NULL DEFAULT 0,
  `best_streak` INT NOT NULL DEFAULT 0,
  `total_claims` INT NOT NULL DEFAULT 0,
  `last_claim_unix` INT NOT NULL DEFAULT 0,
  `last_claim_date` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  KEY `idx_best` (`best_streak`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('daily_streak.enabled', '1'),
  ('daily_streak.milestone_badge', 'ACH_Login7')
ON DUPLICATE KEY UPDATE `key` = `key`;
