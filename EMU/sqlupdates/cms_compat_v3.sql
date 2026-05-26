-- CMS compatibility v3: tutte le colonne e tabelle CMS-only che il sito
-- referenza ma non sono nel base DB Arcturus. Idempotente, safe re-run.

-- users_logins.verified_ip: usata da class.users.php al login.
ALTER TABLE users_logins ADD COLUMN IF NOT EXISTS verified_ip VARCHAR(45) NOT NULL DEFAULT '';

-- Colonne users aggiuntive (dailyreward / accountclient / accountprofilepage / etc).
ALTER TABLE users ADD COLUMN IF NOT EXISTS reward_check         TINYINT(1)   NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS newbie_check         TINYINT(1)   NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS country              VARCHAR(8)   NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN IF NOT EXISTS youtube_embed        VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN IF NOT EXISTS cms_currency_private TINYINT(1)   NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS referrals            INT          NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS hidden               TINYINT(1)   NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS added_by             VARCHAR(64)  NOT NULL DEFAULT '';

-- Tabelle CMS-only usate da dailyreward / events / members / staff pages.
CREATE TABLE IF NOT EXISTS cms_presents_prizes (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  furni_id INT NOT NULL DEFAULT 0,
  name VARCHAR(128) NOT NULL DEFAULT '',
  image VARCHAR(255) NOT NULL DEFAULT '',
  amount INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cms_present_logs (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL DEFAULT '',
  prize VARCHAR(128) NOT NULL DEFAULT '',
  timestamp INT NOT NULL DEFAULT 0,
  KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS events_hosted_logs (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  userid INT NOT NULL DEFAULT 0,
  KEY idx_user (userid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS support_tickets (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  mod_id INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS moderation_tickets (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  moderator_id INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vpn_ip_addresses (
  ip VARCHAR(45) NOT NULL PRIMARY KEY,
  allowed VARCHAR(8) NOT NULL DEFAULT 'true'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vpn_user_whitelist (
  userid INT NOT NULL PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
