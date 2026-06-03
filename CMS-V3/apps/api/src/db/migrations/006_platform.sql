-- =========================================================================
--  CMS-V3 — Platform foundation (Feature Flags + Event Bus)
--
--  Fase 1 del programma "35 feature EMU". Due tabelle-piattaforma su cui si
--  agganceranno molte feature successive (daily streak, happy hour, activity
--  feed, season pass, marketplace v2, ...).
--
--   1. cms_v3_feature_flags — SORGENTE DI VERITÀ dei feature flag.
--      Gestiti dallo staff (rank>=5) via PUT /api/v2/flags/admin/:key;
--      letti da client/web via GET /api/v2/flags; in Fase 2 verranno
--      distribuiti all'EMU via Redis (cache + invalidazione).
--
--   2. cms_v3_activity_events — landing dell'event-bus EMU→CMS
--      (POST /api/v2/activity/ingest, autenticato con secret interno) e
--      sorgente dell'activity feed di community (GET /api/v2/activity/feed).
--      payload JSON, visibilità a livelli (public/friends/staff).
-- =========================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `cms_v3_feature_flags` (
    `key`         VARCHAR(64)         NOT NULL,
    `enabled`     TINYINT(1)          NOT NULL DEFAULT 0,
    `description` VARCHAR(255)        NOT NULL DEFAULT '',
    `rollout_pct` TINYINT(3) UNSIGNED NOT NULL DEFAULT 100,
    `audience`    VARCHAR(24)         NOT NULL DEFAULT 'all',
    `updated_by`  INT(11)             NULL DEFAULT NULL,
    `updated_at`  TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`key`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `cms_v3_activity_events` (
    `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT,
    `actor_id`   INT(11)     NULL DEFAULT NULL,
    `actor_name` VARCHAR(64) NOT NULL DEFAULT '',
    `type`       VARCHAR(48) NOT NULL,
    `payload`    JSON        NULL DEFAULT NULL,
    `visibility` VARCHAR(16) NOT NULL DEFAULT 'public',
    `created_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_feed`  (`visibility`, `created_at`),
    KEY `idx_actor` (`actor_id`, `created_at`),
    KEY `idx_type`  (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed dei flag delle feature in arrivo. Tutte OFF di default: rollout
-- controllato dallo staff. ON DUPLICATE preserva `enabled` (non resetta un
-- flag che lo staff ha già attivato) e aggiorna solo la descrizione.
INSERT INTO `cms_v3_feature_flags` (`key`, `enabled`, `description`, `audience`) VALUES
    ('daily_streak',    0, 'Ricompense streak di login giornaliero',   'all'),
    ('happy_hour',      0, 'Happy Hour automatico (boost crediti/XP)',  'all'),
    ('activity_feed',   0, 'Feed attivita di community',                'all'),
    ('room_follow',     0, 'Segui stanze + notifiche evento',           'all'),
    ('season_pass',     0, 'Battle Pass stagionale',                    'all'),
    ('marketplace_v2',  0, 'Marketplace v2 (aste, storico prezzi)',     'all'),
    ('deep_links',      0, 'Deep-link in-game <-> web',                 'all'),
    ('toxicity_filter', 0, 'Scoring tossicita chat (assistito)',        'staff')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);
