-- =========================================================================
--  CMS-V3 — site-wide settings (key/value store).
--  Sorgente di verità per impostazioni globali del sito: tema attivo,
--  banner di emergenza, ecc. Solo lo staff (rank >= 5) può modificare.
--  Pubblico tutti i visitatori leggono per applicare il tema.
-- =========================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `cms_v3_site_settings` (
    `key`        VARCHAR(64)  NOT NULL,
    `value`      VARCHAR(255) NOT NULL,
    `updated_by` INT(11)      NULL DEFAULT NULL,
    `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed: tema default = habbo-classico (palette habbo.it originale).
INSERT INTO `cms_v3_site_settings` (`key`, `value`) VALUES ('theme', 'habbo-classico')
    ON DUPLICATE KEY UPDATE `key`=`key`;
