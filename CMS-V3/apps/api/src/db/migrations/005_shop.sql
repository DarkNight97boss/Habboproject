-- =========================================================================
--  CMS-V3 — Shop con Stripe Checkout
--
--  Catalogo prodotti (pacchetti crediti, badge esclusivi, abbonamenti HC).
--  Pagamento via Stripe Checkout Session. Delivery automatica via webhook:
--   Stripe checkout.session.completed → CMS-API verifica firma → INSERT order
--   completed → RCON `give_credits {userId} {amount}` via bridge.
--
--  Stripe price_id stored in shop_items.stripe_price_id per stable referencing.
-- =========================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `cms_v3_shop_items` (
    `id`              INT(11) NOT NULL AUTO_INCREMENT,
    `slug`            VARCHAR(64) NOT NULL,
    `name`            VARCHAR(120) NOT NULL,
    `description`     VARCHAR(500) NOT NULL DEFAULT '',
    `category`        VARCHAR(40) NOT NULL DEFAULT 'credits',
    `price_cents`     INT(11) NOT NULL,
    `currency`        VARCHAR(3) NOT NULL DEFAULT 'EUR',
    `credits_amount`  INT(11) NOT NULL DEFAULT 0,
    `diamonds_amount` INT(11) NOT NULL DEFAULT 0,
    `image_gradient`  VARCHAR(200) NOT NULL DEFAULT '',
    `badge_code`      VARCHAR(32) NULL DEFAULT NULL,
    `featured`        TINYINT(1) NOT NULL DEFAULT 0,
    `sort_order`      INT(11) NOT NULL DEFAULT 100,
    `stripe_price_id` VARCHAR(128) NULL DEFAULT NULL,
    `active`          TINYINT(1) NOT NULL DEFAULT 1,
    `created_at`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_slug` (`slug`),
    KEY `idx_active_sort` (`active`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `cms_v3_shop_orders` (
    `id`                   INT(11) NOT NULL AUTO_INCREMENT,
    `user_id`              INT(11) NOT NULL,
    `item_id`              INT(11) NOT NULL,
    `status`               ENUM('pending', 'paid', 'failed', 'refunded') NOT NULL DEFAULT 'pending',
    `amount_cents`         INT(11) NOT NULL,
    `currency`             VARCHAR(3) NOT NULL DEFAULT 'EUR',
    `stripe_session_id`    VARCHAR(255) NULL DEFAULT NULL,
    `stripe_payment_intent` VARCHAR(255) NULL DEFAULT NULL,
    `delivered_at`         TIMESTAMP NULL DEFAULT NULL,
    `delivery_log`         VARCHAR(500) NULL DEFAULT NULL,
    `created_at`           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `paid_at`              TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`),
    UNIQUE KEY `uniq_stripe_session` (`stripe_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
