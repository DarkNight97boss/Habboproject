-- CMS compatibility v4: full schema migration from legacy `habbo` DB to `ms`.
--
-- Background: il CMS Zabbo girava storicamente su DB `habbo` (user arcturus),
-- separato da quello dell'emulatore (`ms`, user root). Quando il CMS è stato
-- riallineato sullo stesso DB dell'emu, mancavano:
--   * 23 tabelle CMS-only / wired / template
--   * 8 colonne `users`
--   * 5 colonne `rooms` / `users_settings`
--
-- Questo file aggrega TUTTE le ALTER + CREATE necessarie. Idempotente.

-- 1) 8 colonne `users` extra (auth ticket TTL, remember-me, background card, etc).
ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_ticket_expires_at    TIMESTAMP        NULL DEFAULT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS remember_token_hash       VARCHAR(64)      NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN IF NOT EXISTS remember_token_expires_at INT(11) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS background_id             INT(11)          NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS background_stand_id       INT(11)          NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS background_overlay_id     INT(11)          NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS background_card_id        INT(11)          NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_username_change      INT(11)          NOT NULL DEFAULT 0;

-- 2) Colonne `rooms` / `users_settings` usate dal builders_club + youtube widgets.
ALTER TABLE rooms          ADD COLUMN IF NOT EXISTS allow_underpass              ENUM('0','1') NOT NULL DEFAULT '0';
ALTER TABLE rooms          ADD COLUMN IF NOT EXISTS builders_club_trial_locked   TINYINT(1)    NOT NULL DEFAULT 0;
ALTER TABLE rooms          ADD COLUMN IF NOT EXISTS builders_club_original_state VARCHAR(16)   NOT NULL DEFAULT '';
ALTER TABLE rooms          ADD COLUMN IF NOT EXISTS youtube_enabled              TINYINT(1)    NOT NULL DEFAULT 0;
ALTER TABLE users_settings ADD COLUMN IF NOT EXISTS builders_club_bonus_furni    INT(11)       NOT NULL DEFAULT 0;

-- 3) 23 tabelle CMS-only mancanti (schema only; dati seed sotto).
-- NB: per brevita' qui crediamo solo gli schemi "leggeri". Le strutture esatte
-- complete sono state importate al volo via:
--   mysqldump --no-data habbo <table_list> | sed 's/CREATE TABLE/CREATE TABLE IF NOT EXISTS/'
-- Se reinstalli il DB da zero, ripeti quel comando.

CREATE TABLE IF NOT EXISTS permission_ranks (
  id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  rank_name VARCHAR(60) NOT NULL DEFAULT '',
  description TEXT,
  badge_id VARCHAR(40) NOT NULL DEFAULT '',
  room_effect INT(11) NOT NULL DEFAULT 0,
  prefix VARCHAR(40) NOT NULL DEFAULT '',
  prefix_color VARCHAR(7) NOT NULL DEFAULT '',
  enabled ENUM('1','0') NOT NULL DEFAULT '1',
  log_staff_actions ENUM('1','0') NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS permission_definitions (
  rank_id INT(11) NOT NULL,
  permission_name VARCHAR(64) NOT NULL,
  setting ENUM('disallowed','allowed','room_owner') NOT NULL DEFAULT 'disallowed',
  PRIMARY KEY (rank_id, permission_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS cms_news (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL DEFAULT '',
  shortstory TEXT,
  longstory MEDIUMTEXT,
  topstory_image VARCHAR(255) NOT NULL DEFAULT '',
  image VARCHAR(255) NOT NULL DEFAULT '',
  author VARCHAR(64) NOT NULL DEFAULT '',
  category VARCHAR(64) NOT NULL DEFAULT '',
  timestamp INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS password_resets (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  token VARCHAR(128) NOT NULL,
  expires INT NOT NULL DEFAULT 0,
  used TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_token (token),
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS infostand_backgrounds (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL DEFAULT '',
  image VARCHAR(255) NOT NULL DEFAULT '',
  enabled TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- (le altre 18 tabelle CMS-only — builders_club_items, custom_*, room_templates,
--  room_*_wired_*, user_*, wired_emulator_settings — non sono usate dalla home
--  o dal /client direttamente, ma esistono sulla copia migrate live di ms.)
