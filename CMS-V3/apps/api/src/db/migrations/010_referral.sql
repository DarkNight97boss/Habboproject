-- 010_referral.sql — Referral (#15): codice invito + riscatto con premio a entrambi.
--
-- DARK-LAUNCH: il flag `referral` nasce SPENTO. La feature NON tocca il flusso
-- di registrazione: l'invitato riscatta un codice DOPO essersi registrato
-- (POST /api/v2/referral/redeem), con anti-abuso (solo account recenti, una
-- volta sola, niente auto-referral). Premio crediti via RCON a chi invita e a
-- chi riscatta.
--
-- Idempotente (CREATE IF NOT EXISTS + INSERT ON DUPLICATE).

CREATE TABLE IF NOT EXISTS `cms_v3_referral_codes` (
    `user_id`    INT          NOT NULL,
    `code`       VARCHAR(16)  NOT NULL,
    `created_at` BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uq_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `cms_v3_referrals` (
    `referred_id`    INT          NOT NULL,          -- chi riscatta (1 sola volta → PK)
    `referrer_id`    INT          NOT NULL,          -- proprietario del codice
    `code`           VARCHAR(16)  NOT NULL,
    `reward_credits` INT          NOT NULL DEFAULT 0,
    `created_at`     BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`referred_id`),
    KEY `idx_referrer` (`referrer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Flag spento di default (dark-launch). Audience 'all'.
INSERT INTO `cms_v3_feature_flags` (`key`, `enabled`, `description`, `audience`) VALUES
    ('referral', 0, 'Programma referral: codice invito + premio a chi invita e a chi lo riscatta', 'all')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);
