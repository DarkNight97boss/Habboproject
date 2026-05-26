-- Anti-flood chat guard config keys. See com.eu.habbo.core.ChatSpamGuard.
-- No persistent table needed: this is a runtime guard. Real sanctions live in
-- the existing mute table (via habbo.mute(...)) and audit_log AUTO_MUTE_SPAM.

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('chat.spam.enabled', '1'),
  ('chat.spam.rate.window_ms', '10000'),
  ('chat.spam.rate.threshold', '8'),
  ('chat.spam.repeat.threshold', '4'),
  ('chat.spam.mute.base_seconds', '120'),
  ('chat.spam.escalation.reset_seconds', '86400')
ON DUPLICATE KEY UPDATE `key` = `key`;
