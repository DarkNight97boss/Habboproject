-- Device fingerprint registry (anti-multiaccount / ban-evasion detection).
-- The client computes a canvas+UA+screen+timezone signature and sends it once
-- per session; the server SHA-256s the canonical payload and persists it here.
-- See com.eu.habbo.core.DeviceFingerprint.

CREATE TABLE IF NOT EXISTS `user_fingerprints` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `fingerprint_hash` CHAR(64) NOT NULL,
  `ip` VARCHAR(45) NOT NULL DEFAULT '',
  `machine_id` VARCHAR(128) NOT NULL DEFAULT '',
  `user_agent` VARCHAR(512) NOT NULL DEFAULT '',
  `first_seen` INT NOT NULL,
  `last_seen` INT NOT NULL,
  `seen_count` INT NOT NULL DEFAULT 1,
  UNIQUE KEY `uniq_user_fp` (`user_id`, `fingerprint_hash`),
  KEY `idx_fp` (`fingerprint_hash`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('fingerprint.enabled', '1'),
  ('fingerprint.alert_min_users', '2'),
  ('fingerprint.alert_window_days', '30')
ON DUPLICATE KEY UPDATE `key` = `key`;
