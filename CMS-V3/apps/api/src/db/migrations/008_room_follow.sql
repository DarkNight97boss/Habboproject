-- =========================================================================
--  CMS-V3 — Stanze seguite / preferite (Fase 3, feature #12)
--
--  L'utente "segue" una stanza (preferito). Lista + live count sulla /me e
--  toggle sulla pagina /community/rooms. Gated dietro il flag `room_follow`.
--  La notifica "evento nella stanza seguita" arriverà col ponte EMU (Fase 2).
--
--  room_id = rooms.id Arcturus (niente FK verso `rooms`: engine upstream).
-- =========================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `cms_v3_room_follows` (
    `user_id`    INT(11)   NOT NULL,
    `room_id`    INT(11)   NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`, `room_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
