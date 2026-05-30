-- =========================================================================
--  CMS-V3 — schema iniziale.
--  Tutte le tabelle hanno prefisso cms_v3_ per evitare collisioni con il
--  vecchio CMS PHP che vive nello stesso DB `ms`.
-- =========================================================================

SET NAMES utf8mb4;

-- Refresh token store con rotation family.
-- Quando un token viene usato genera (token nuovo + invalida quello usato);
-- se vediamo un token già usato → potenziale furto → revochiamo l'INTERA family.
CREATE TABLE IF NOT EXISTS `cms_v3_refresh_tokens` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` INT(11) NOT NULL,
    `family` CHAR(32) NOT NULL,
    `jti` CHAR(32) NOT NULL,
    `created_at` INT(11) NOT NULL,
    `expires_at` INT(11) NOT NULL,
    `used_at` INT(11) NULL DEFAULT NULL,
    `revoked_at` INT(11) NULL DEFAULT NULL,
    `replaced_by_jti` CHAR(32) NULL DEFAULT NULL,
    `ip` VARCHAR(45) NULL,
    `user_agent` VARCHAR(255) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_jti` (`jti`),
    KEY `idx_family` (`family`),
    KEY `idx_user` (`user_id`),
    KEY `idx_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit log azioni sensibili (login, password change, ban, ecc.)
-- Già esiste audit_log (EMU) ma quella è hash-chain firmata e non usata
-- direttamente dal CMS. Qui teniamo log applicativo separato.
CREATE TABLE IF NOT EXISTS `cms_v3_audit_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` INT(11) NULL,
    `action` VARCHAR(64) NOT NULL,
    `target_type` VARCHAR(64) NULL,
    `target_id` BIGINT NULL,
    `ip` VARCHAR(45) NULL,
    `user_agent` VARCHAR(255) NULL,
    `details` JSON NULL,
    `created_at` INT(11) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_action` (`action`),
    KEY `idx_created` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabella opzionale per limitare cooldown registrazione per IP/email.
CREATE TABLE IF NOT EXISTS `cms_v3_register_attempts` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `ip` VARCHAR(45) NOT NULL,
    `email_hash` CHAR(64) NULL,
    `created_at` INT(11) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_ip_created` (`ip`, `created_at` DESC),
    KEY `idx_email` (`email_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
