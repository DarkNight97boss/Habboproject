-- Recovery codes monouso per lo staff MFA.
-- Risolve il caso "ho perso il telefono / autenticatore": l'utente puo' redimere
-- uno dei codici al posto del TOTP. Ogni codice e' valido una volta sola.
-- Lo storage e' SHA-256 della forma normalizzata (uppercase, senza '-'); i
-- codici in chiaro vivono SOLO nella sessione di generazione.

CREATE TABLE IF NOT EXISTS `staff_mfa_recovery_codes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `code_hash` CHAR(64) NOT NULL,
  `created_at` INT NOT NULL,
  `used_at` INT NULL,
  UNIQUE KEY `uniq_user_hash` (`user_id`, `code_hash`),
  KEY `idx_user_unused` (`user_id`, `used_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Config keys.
INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('mfa.staff.recovery.enabled', '1'),
  ('mfa.staff.recovery.count', '8')
ON DUPLICATE KEY UPDATE `key` = `key`;
