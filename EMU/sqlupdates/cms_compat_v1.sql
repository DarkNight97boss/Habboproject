-- CMS compatibility columns: tables/columns referenced by the Zabbo CMS
-- (PHP templates under CMS/app/tpl/skins/ZabboME/) that are NOT created by the
-- Arcturus 3.5.5 base DB. Without them, /client and /me throw mysql_query
-- errors:
--   users.client_menu     -- client.php WHERE client_menu = '0'
--   users.account_locked  -- includes/checktheban.php WHERE account_locked='1'
--   users.access          -- includes/maintenance_access.php WHERE access='1'
--   users.verified_ip     -- client.php $row['verified_ip']
--
-- Safe defaults so the user flow doesn't break: 0/1 according to the check
-- direction (e.g. access=1 means "allowed during maintenance"; account_locked=0
-- means "not locked").

ALTER TABLE users ADD COLUMN IF NOT EXISTS client_menu    TINYINT(1)  NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked TINYINT(1)  NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS access         TINYINT(1)  NOT NULL DEFAULT 1;
ALTER TABLE users ADD COLUMN IF NOT EXISTS verified_ip    VARCHAR(45) NOT NULL DEFAULT '';
