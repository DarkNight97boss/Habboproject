-- 009_shadow_mute.sql — Shadow-mute (#19): "mute ombra" gestita dallo staff.
--
-- DARK-LAUNCH: il flag `shadow_mute` nasce SPENTO e la tabella nasce vuota,
-- quindi al deploy non cambia nulla in gioco. Quando lo staff accende il flag,
-- l'EMU (poller core/ShadowMute) legge questo set e nasconde i messaggi
-- TALK/SHOUT dell'utente a tutti gli occupanti tranne lui e lo staff.
--
-- Idempotente (CREATE IF NOT EXISTS + INSERT ON DUPLICATE).

CREATE TABLE IF NOT EXISTS `cms_v3_shadow_mutes` (
    `user_id`    INT           NOT NULL,                 -- id utente (users.id)
    `until_ts`   BIGINT        NOT NULL DEFAULT 0,       -- 0 = permanente; altrimenti unix di scadenza
    `reason`     VARCHAR(255)  NOT NULL DEFAULT '',
    `created_by` INT           NOT NULL DEFAULT 0,       -- staff che ha applicato la mute
    `created_at` BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`),
    KEY `idx_until` (`until_ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Flag spento di default (dark-launch). Audience 'all' cosi' che l'EMU (CmsFlags,
-- che legge solo i flag non-staff ATTIVI) lo veda quando lo staff lo accende.
-- ON DUPLICATE preserva `enabled` (non resetta un flag gia' impostato dallo staff).
INSERT INTO `cms_v3_feature_flags` (`key`, `enabled`, `description`, `audience`) VALUES
    ('shadow_mute', 0, 'Mute ombra: i messaggi dell''utente sono visibili solo a lui e allo staff', 'all')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);
