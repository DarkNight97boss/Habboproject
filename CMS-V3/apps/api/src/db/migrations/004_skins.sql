-- =========================================================================
--  CMS-V3 — Skin Engine: libreria di "skin" (tema completo).
--
--  Uno skin è MOLTO più di un set di colori: include tipografia, spacing,
--  bordi, ombre, animazioni, asset (logo/bg), layout structure. È salvato
--  come manifest JSON in `manifest_json` (vedi shape in lib/skin.ts).
--
--  - `is_builtin=1`  → skin distribuito con il codice (3 iniziali). Non
--                       cancellabile dallo staff.
--  - `is_custom=1`   → skin creato dallo staff via Theme Builder (Fase 4).
--                       Cancellabile dal suo creatore o da admin.
--
--  Lo skin attivo è in `cms_v3_site_settings` con key='active_skin_slug'.
--  Tenere lì il riferimento (non come FK in skins.is_active) ci permette
--  storia di chi cambia + rollback rapido.
-- =========================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `cms_v3_skins` (
    `id`            INT(11)        NOT NULL AUTO_INCREMENT,
    `slug`          VARCHAR(64)    NOT NULL,
    `name`          VARCHAR(100)   NOT NULL,
    `description`   VARCHAR(500)   NOT NULL DEFAULT '',
    `manifest_json` LONGTEXT       NOT NULL,
    `is_builtin`    TINYINT(1)     NOT NULL DEFAULT 0,
    `is_custom`     TINYINT(1)     NOT NULL DEFAULT 0,
    `created_by`    INT(11)        NULL DEFAULT NULL,
    `created_at`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_slug` (`slug`),
    KEY `idx_builtin` (`is_builtin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Default: lo skin attivo viene salvato come site_setting per consentire
-- override veloci senza scrivere su skins (audit-friendly).
INSERT INTO `cms_v3_site_settings` (`key`, `value`)
VALUES ('active_skin_slug', 'habbo-classico-2008')
ON DUPLICATE KEY UPDATE `key`=`key`;

-- I 3 skin builtin sono seedati lato applicazione (POST /api/v2/skins/seed
-- — endpoint admin) per evitare di duplicare i JSON nel SQL e nel TS.
-- Vedi services/skin-seed.ts.
