-- SSO auth_ticket TTL — server-side guard against stale/leaked tickets.
-- The CMS should write `auth_ticket_issued_at = UNIX_TIMESTAMP()` whenever it
-- mints a new SSO ticket; the EMU then rejects tickets older than
-- `sso.ticket.ttl.seconds` (default 60s). When the column is 0 the EMU does
-- NOT enforce the TTL — keeps the change backward-compatible with CMS builds
-- that have not been updated yet.

SET @col := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'auth_ticket_issued_at');
SET @sql := IF(@col = 0, 'ALTER TABLE `users` ADD COLUMN `auth_ticket_issued_at` INT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO `emulator_settings` (`key`, `value`) VALUES
  ('sso.ticket.ttl.seconds', '60')
ON DUPLICATE KEY UPDATE `key` = `key`;
