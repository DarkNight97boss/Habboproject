-- Staff step-up MFA (Google Authenticator / RFC-6238 TOTP).
-- Locks staff powers after login until a valid 6-digit code is entered in a
-- Habbo-style popup. Anti-account-theft. See com.eu.habbo.core.StaffMfa / Totp.
-- Feature is config-gated OFF by default (mfa.staff.enabled = 0).

CREATE TABLE IF NOT EXISTS `staff_mfa` (
  `user_id` INT NOT NULL,
  `secret` VARCHAR(64) NOT NULL DEFAULT '',
  `enrolled` TINYINT(1) NOT NULL DEFAULT 0,
  `enrolled_at` INT NOT NULL DEFAULT 0,
  `last_counter` BIGINT NOT NULL DEFAULT 0,
  `last_used_at` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Config keys (inserted so ConfigurationManager doesn't log missing-key errors).
INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('mfa.staff.enabled', '0'),
  ('mfa.staff.min_rank', '0'),
  ('mfa.staff.issuer', 'Habbo Hotel')
ON DUPLICATE KEY UPDATE `key` = `key`;
