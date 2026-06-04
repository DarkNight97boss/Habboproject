-- 014_economy_log.sql — Telemetria economia (#8): log dei grant di valuta
-- emessi dal CMS (faucet: streak, referral, missioni, season pass, ...).
--
-- Consente al pannello staff di vedere quanto immettono i rubinetti nel tempo.
-- Solo i grant CMS-side (quelli che controlliamo); i flussi interni all'EMU
-- (scambi, shop in-game) non passano da qui. Idempotente.

CREATE TABLE IF NOT EXISTS `cms_v3_economy_log` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    INT          NOT NULL,
    `currency`   VARCHAR(16)  NOT NULL DEFAULT 'credits',
    `amount`     INT          NOT NULL,
    `source`     VARCHAR(48)  NOT NULL,
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_source_time` (`source`, `created_at`),
    KEY `idx_time` (`created_at`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
