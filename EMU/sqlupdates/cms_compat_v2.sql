-- CMS compatibility v2: tables + columns the Zabbo CMS uses that are NOT in
-- the base Arcturus 3.5.5 DB. Without these, /client (and many CMS pages)
-- throw mysql_query errors as soon as header.php / class.core.php / class.users.php
-- touch the missing tables.
--
-- All idempotent. Safe to re-run.

-- ============ users: missing columns ============
ALTER TABLE users ADD COLUMN IF NOT EXISTS shopbought       TINYINT(1)  NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS shoppackage      VARCHAR(64) NULL DEFAULT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS security_enabled TINYINT(1)  NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS pin              VARCHAR(8)  NOT NULL DEFAULT '';

-- ============ bans: missing column ============
-- class.users.php uses `SELECT reason FROM bans` while the EMU schema has
-- `ban_reason`. Add `reason` as a parallel column so reads work.
ALTER TABLE bans ADD COLUMN IF NOT EXISTS reason VARCHAR(255) NOT NULL DEFAULT '';

-- ============ CMS-only tables ============
CREATE TABLE IF NOT EXISTS users_apps (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  reply_sent TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS profile_comments (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  profile_id INT NOT NULL,
  author_id INT NOT NULL DEFAULT 0,
  message TEXT,
  timestamp INT NOT NULL DEFAULT 0,
  KEY idx_profile (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cms_user_reports (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL DEFAULT 0,
  status TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_user (user_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cms_settings (
  `key` VARCHAR(64) NOT NULL PRIMARY KEY,
  `value` VARCHAR(255) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO cms_settings (`key`,`value`) VALUES
  ('vpn_mode','0'),
  ('maintenance','0');

CREATE TABLE IF NOT EXISTS user_secure (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user INT NOT NULL,
  last_ip VARCHAR(45) NOT NULL DEFAULT '',
  KEY idx_user (user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users_logins (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  ip VARCHAR(45) NOT NULL DEFAULT '',
  timestamp INT NOT NULL DEFAULT 0,
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS server_status (
  status TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO server_status (status) VALUES (1);

CREATE TABLE IF NOT EXISTS friendships (
  user_one INT NOT NULL,
  user_two INT NOT NULL,
  PRIMARY KEY (user_one, user_two)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
