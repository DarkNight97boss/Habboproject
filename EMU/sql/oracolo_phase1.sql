-- =========================================================================
--  Bot Oracolo — Fase 1
--  Raccolta richieste di nuove feature da utenti nella stanza Oracolo.
--  La Fase 2 (generazione bozza spec via LLM) usa il campo ai_draft.
-- =========================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `feature_requests` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `user_id` INT(11) NOT NULL,
    `user_name` VARCHAR(64) NOT NULL DEFAULT '',
    `room_id` INT(11) NOT NULL DEFAULT 0,
    `text` VARCHAR(500) NOT NULL DEFAULT '',
    `status` ENUM('PENDING','IN_REVIEW','ACCEPTED','REJECTED','IMPLEMENTED') NOT NULL DEFAULT 'PENDING',
    `votes_up` INT(11) NOT NULL DEFAULT 0,
    `votes_down` INT(11) NOT NULL DEFAULT 0,
    `ai_draft` TEXT NULL DEFAULT NULL,
    `ai_draft_at` INT(11) NULL DEFAULT NULL,
    `created_at` INT(11) NOT NULL DEFAULT 0,
    `updated_at` INT(11) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_user` (`user_id`),
    KEY `idx_created` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `feature_request_votes` (
    `request_id` INT(11) NOT NULL,
    `user_id` INT(11) NOT NULL,
    `vote` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '+1 upvote, -1 downvote',
    `voted_at` INT(11) NOT NULL DEFAULT 0,
    PRIMARY KEY (`request_id`, `user_id`),
    KEY `idx_request` (`request_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Config per il bot (room_id può essere cambiato senza ricompilare l'EMU).
INSERT INTO `emulator_settings` (`key`, `value`) VALUES
    ('oracolo.enabled', '1'),
    ('oracolo.room_id', '58'),
    ('oracolo.max_text_length', '300'),
    ('oracolo.cooldown_seconds', '30'),
    ('oracolo.llm.provider', 'claude'),
    ('oracolo.llm.api_key', ''),
    ('oracolo.llm.model', 'claude-sonnet-4-5')
ON DUPLICATE KEY UPDATE `value`=VALUES(`value`);

-- Comando :oracolo / :idea / :proposta — visibile a tutti.
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
    ('commands.keys.cmd_oracolo', 'oracolo;idea;proposta'),
    ('commands.description.cmd_oracolo', ':oracolo [lista|vota <id>|accetta <id>|rifiuta <id>|inreview <id>]'),
    ('commands.error.cmd_oracolo.no_subcommand', 'Usa: :oracolo lista — oppure scrivi in chat nella stanza Oracolo per proporre una feature.'),
    ('commands.error.cmd_oracolo.invalid_id', 'ID richiesta non valido. Usa :oracolo lista per vedere gli ID.'),
    ('commands.error.cmd_oracolo.not_found', 'Richiesta #%id% non trovata.'),
    ('commands.error.cmd_oracolo.staff_only', 'Solo lo staff può cambiare lo stato delle richieste.'),
    ('commands.error.cmd_oracolo.cooldown', 'Hai già proposto una feature di recente. Riprova tra %seconds% secondi.'),
    ('commands.error.cmd_oracolo.too_short', 'La proposta deve avere almeno 10 caratteri.'),
    ('commands.error.cmd_oracolo.too_long', 'La proposta non può superare %max% caratteri.'),
    ('commands.error.cmd_oracolo.already_voted', 'Hai già votato questa richiesta.'),
    ('commands.error.cmd_oracolo.own_request', 'Non puoi votare la tua stessa proposta.'),
    ('commands.success.cmd_oracolo.received', '✨ Grazie! La tua proposta è stata registrata come richiesta #%id%. Lo staff la valuterà presto.'),
    ('commands.success.cmd_oracolo.voted', 'Hai votato la richiesta #%id%. Punteggio attuale: +%up% / -%down%.'),
    ('commands.success.cmd_oracolo.status_changed', 'Stato richiesta #%id% aggiornato a: %status%.'),
    ('commands.info.cmd_oracolo.list_header', '🔮 Bacheca Idee Oracolo — Top richieste:'),
    ('commands.info.cmd_oracolo.list_empty', 'Nessuna richiesta in bacheca. Sii il primo a proporre qualcosa nella stanza Oracolo!'),
    ('commands.info.cmd_oracolo.list_entry', '#%id% [%status%] +%up%/-%down% — %text% (di %user%)')
ON DUPLICATE KEY UPDATE `value`=VALUES(`value`);

SELECT 'Oracolo schema applied' AS done;
